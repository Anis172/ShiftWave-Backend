package com.example.restaurantshifthandler.controller;

import com.example.restaurantshifthandler.dto.ShiftDTO;
import com.example.restaurantshifthandler.entity.Shift;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
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
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();
        List<Shift> shifts = service.findByRestaurantId(restaurantId);

        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shift> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/worker/{workerId}")
    public List<Shift> getByWorker(@PathVariable Long workerId) {
        return service.findByWorker(workerId);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<Shift> getByRestaurant(@PathVariable Long restaurantId) {
        return service.findByRestaurant(restaurantId);
    }

    @GetMapping("/status/{status}")
    public List<Shift> getByStatus(@PathVariable ShiftStatus status) {
        return service.findByStatus(status);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ShiftDTO dto, BindingResult result, Authentication authentication) {


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


        Optional<User> workerOpt = userService.findById(dto.getWorkerId());
        if (workerOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Worker not found"));
        }

        User worker = workerOpt.get();


        if (!worker.getIsActive()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot create shift for inactive worker. Please activate the worker first."));
        }


        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();

        if (!worker.getRestaurant().getId().equals(restaurantId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Worker does not belong to your restaurant"));
        }


        Shift createdShift = service.save(dto, restaurantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShift);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ShiftDTO dto, BindingResult result, Authentication authentication) {


        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }


        Optional<Shift> existingShiftOpt = service.findById(id);
        if (existingShiftOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Shift not found"));
        }


        if (dto.getScheduledEnd().isBefore(dto.getScheduledStart()) ||
                dto.getScheduledEnd().isEqual(dto.getScheduledStart())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "End time must be after start time"));
        }


        Optional<User> workerOpt = userService.findById(dto.getWorkerId());
        if (workerOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Worker not found"));
        }

        User worker = workerOpt.get();


        if (!worker.getIsActive()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot assign shift to inactive worker"));
        }


        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

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


        Optional<Shift> shiftOpt = service.findById(id);
        if (shiftOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Shift not found"));
        }

        Shift shift = shiftOpt.get();


        if (!shift.getWorker().getIsActive()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Your account is inactive. Please contact your manager."));
        }

        Shift updatedShift = service.clockIn(id);
        return ResponseEntity.ok(updatedShift);
    }

    @PatchMapping("/{id}/clock-out")
    public ResponseEntity<?> clockOut(@PathVariable Long id) {


        Optional<Shift> shiftOpt = service.findById(id);
        if (shiftOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Shift not found"));
        }

        Shift updatedShift = service.clockOut(id);
        return ResponseEntity.ok(updatedShift);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {


        Optional<Shift> shiftOpt = service.findById(id);
        if (shiftOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Shift not found"));
        }

        Shift cancelledShift = service.cancel(id);
        return ResponseEntity.ok(cancelledShift);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {


        Optional<Shift> shiftOpt = service.findById(id);
        if (shiftOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Shift not found"));
        }

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}