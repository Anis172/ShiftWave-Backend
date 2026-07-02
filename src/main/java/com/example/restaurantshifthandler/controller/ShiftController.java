package com.example.restaurantshifthandler.controller;

import com.example.restaurantshifthandler.dto.ShiftDTO;
import com.example.restaurantshifthandler.entity.Shift;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import com.example.restaurantshifthandler.exception.ResourceNotFoundException;
import com.example.restaurantshifthandler.service.ShiftService;
import com.example.restaurantshifthandler.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService service;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Shift>> getAll(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();
        List<Shift> shifts = service.findByRestaurantId(restaurantId);

        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
    }

    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<Shift>> getByWorker(@PathVariable Long workerId) {
        return ResponseEntity.ok(service.findByWorker(workerId));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Shift>> getByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(service.findByRestaurant(restaurantId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Shift>> getByStatus(@PathVariable ShiftStatus status) {
        return ResponseEntity.ok(service.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody ShiftDTO dto,
            BindingResult result,
            Authentication authentication) {

        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        if (dto.getScheduledEnd().isBefore(dto.getScheduledStart()) ||
                dto.getScheduledEnd().isEqual(dto.getScheduledStart())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "End time must be after start time"));
        }

        User worker = userService.findById(dto.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        if (!worker.getIsActive()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot create shift for inactive worker"));
        }

        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();

        if (!worker.getRestaurant().getId().equals(restaurantId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Worker does not belong to your restaurant"));
        }

        Shift createdShift = service.save(dto, restaurantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShift);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody ShiftDTO dto,
            BindingResult result,
            Authentication authentication) {

        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));

        if (dto.getScheduledEnd().isBefore(dto.getScheduledStart()) ||
                dto.getScheduledEnd().isEqual(dto.getScheduledStart())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "End time must be after start time"));
        }

        User worker = userService.findById(dto.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        if (!worker.getIsActive()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot assign shift to inactive worker"));
        }

        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();

        if (!worker.getRestaurant().getId().equals(restaurantId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Worker does not belong to your restaurant"));
        }

        Shift updatedShift = service.update(id, dto, restaurantId);
        return ResponseEntity.ok(updatedShift);
    }

    @PatchMapping("/{id}/clock-in")
    public ResponseEntity<?> clockIn(@PathVariable Long id) {
        Shift shift = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));

        if (!shift.getWorker().getIsActive()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Your account is inactive. Please contact your manager."));
        }

        return ResponseEntity.ok(service.clockIn(id));
    }

    @PatchMapping("/{id}/clock-out")
    public ResponseEntity<?> clockOut(@PathVariable Long id) {
        service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));

        return ResponseEntity.ok(service.clockOut(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));

        return ResponseEntity.ok(service.cancel(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}