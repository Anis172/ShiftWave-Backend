package com.example.restaurantshifthandler.controller;

import com.example.restaurantshifthandler.dto.BreakRequestDTO;
import com.example.restaurantshifthandler.entity.BreakRequest;
import com.example.restaurantshifthandler.entity.Shift;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.enums.BreakStatus;
import com.example.restaurantshifthandler.mapper.BreakRequestMapper;
import com.example.restaurantshifthandler.service.BreakRequestService;
import com.example.restaurantshifthandler.service.ShiftService;
import com.example.restaurantshifthandler.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/break-requests")
@RequiredArgsConstructor
public class BreakRequestController {

    private final BreakRequestService service;
    private final BreakRequestMapper mapper;
    private final UserService userService;
    private final ShiftService shiftService;

    @GetMapping
    public ResponseEntity<List<BreakRequest>> getAll(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();
        List<BreakRequest> breaks = service.findByRestaurantId(restaurantId);

        return ResponseEntity.ok(breaks);
    }
     
@GetMapping("/{id}")
public ResponseEntity<BreakRequestDTO> getById(@PathVariable Long id) {
    return service.findById(id)
            .map(mapper::toDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}

    @GetMapping("/shift/{shiftId}")
    public List<BreakRequest> getByShift(@PathVariable Long shiftId) {
        return service.findByShift(shiftId);
    }

    @GetMapping("/worker/{workerId}")
    public List<BreakRequest> getByWorker(@PathVariable Long workerId) {
        return service.findByWorker(workerId);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BreakRequest>> getByStatus(
            @PathVariable BreakStatus status,
            Authentication authentication) {

        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();

        List<BreakRequest> breaks = service.findByRestaurantId(restaurantId)
                .stream()
                .filter(br -> br.getStatus() == status)
                .collect(Collectors.toList());

        return ResponseEntity.ok(breaks);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody BreakRequestDTO dto, BindingResult result) {

        // ✅ Check DTO validation errors
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        // ✅ Check: Worker exists
        Optional<User> workerOpt = userService.findById(dto.getWorkerId());
        if (workerOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Worker not found"));
        }

        User worker = workerOpt.get();

        // ✅ Check: Worker is active
        if (!worker.getIsActive()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Your account is inactive. Please contact your manager."));
        }

        // ✅ Check: Shift exists
        Optional<Shift> shiftOpt = shiftService.findById(dto.getShiftId());
        if (shiftOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Shift not found"));
        }

        Shift shift = shiftOpt.get();

        // ✅ Check: Worker owns this shift
        if (!shift.getWorker().getId().equals(dto.getWorkerId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You can only request breaks for your own shifts"));
        }

        // ✅ Check: Shift is ACTIVE (can't request break for future/completed shifts)
        if (!shift.getStatus().toString().equals("ACTIVE")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You can only request breaks during active shifts"));
        }

        // Convert DTO to entity
        BreakRequest breakRequest = mapper.toEntity(dto);

        // ✅ All good, create break request (service will check coverage)
        try {
            BreakRequest createdBreak = service.save(breakRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBreak);
        } catch (RuntimeException e) {
            // Coverage check failed in service
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody BreakRequestDTO dto, BindingResult result) {

        // ✅ Check DTO validation errors
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        // ✅ Check: Break request exists
        Optional<BreakRequest> breakOpt = service.findById(id);
        if (breakOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Break request not found"));
        }

        // Convert DTO to entity
        BreakRequest data = mapper.toEntity(dto);

        BreakRequest updatedBreak = service.update(id, data);
        return ResponseEntity.ok(updatedBreak);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {

        // ✅ Check: Break request exists
        Optional<BreakRequest> breakOpt = service.findById(id);
        if (breakOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Break request not found"));
        }

        try {
            BreakRequest approvedBreak = service.approve(id);
            return ResponseEntity.ok(approvedBreak);
        } catch (RuntimeException e) {
            // Coverage check failed
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/deny")
    public ResponseEntity<?> deny(@PathVariable Long id) {

        // ✅ Check: Break request exists
        Optional<BreakRequest> breakOpt = service.findById(id);
        if (breakOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Break request not found"));
        }

        BreakRequest deniedBreak = service.deny(id);
        return ResponseEntity.ok(deniedBreak);
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<?> start(@PathVariable Long id) {

        // ✅ Check: Break request exists
        Optional<BreakRequest> breakOpt = service.findById(id);
        if (breakOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Break request not found"));
        }

        BreakRequest startedBreak = service.start(id);
        return ResponseEntity.ok(startedBreak);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id) {

        // ✅ Check: Break request exists
        Optional<BreakRequest> breakOpt = service.findById(id);
        if (breakOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Break request not found"));
        }

        BreakRequest completedBreak = service.complete(id);
        return ResponseEntity.ok(completedBreak);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        // ✅ Check: Break request exists
        Optional<BreakRequest> breakOpt = service.findById(id);
        if (breakOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Break request not found"));
        }

        BreakRequest breakRequest = breakOpt.get();

        // ✅ Check: Can only delete APPROVED breaks (not ACTIVE or COMPLETED)
        if (breakRequest.getStatus() == BreakStatus.ACTIVE ||
                breakRequest.getStatus() == BreakStatus.COMPLETED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot delete active or completed breaks"));
        }

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<BreakRequestDTO>> getBreakHistory(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();
        List<BreakRequest> completedBreaks = service.findByRestaurantIdAndStatusOrderByStartTimeDesc(restaurantId, BreakStatus.COMPLETED);

        List<BreakRequestDTO> dtos = completedBreaks.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
