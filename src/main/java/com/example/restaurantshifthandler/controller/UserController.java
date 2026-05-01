package com.example.restaurantshifthandler.controller;

import com.example.restaurantshifthandler.dto.UserDTO;
import com.example.restaurantshifthandler.entity.User;
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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<List<User>> getAll(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = service.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();
        List<User> users = service.findByRestaurantId(restaurantId);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<User> getByRestaurant(@PathVariable Long restaurantId) {
        return service.findByRestaurant(restaurantId);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserDTO dto, BindingResult result, Authentication authentication) {
        // ✅ Check DTO validation errors
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        try {
            // ✅ Get current user's restaurant
            String email = authentication.getName();
            User currentUser = service.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long restaurantId = currentUser.getRestaurant().getId();

            // ✅ Service handles all validation logic
            User createdUser = service.save(dto, restaurantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto, BindingResult result, Authentication authentication) {
        // ✅ Check DTO validation errors
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        try {
            // ✅ Get current user's restaurant
            String email = authentication.getName();
            User currentUser = service.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long restaurantId = currentUser.getRestaurant().getId();

            // ✅ Service handles all validation logic
            User updatedUser = service.update(id, dto, restaurantId);
            return ResponseEntity.ok(updatedUser);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = service.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Service handles validation (self-deletion, exists check)
            service.deleteById(id, currentUser.getId());
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = service.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Service handles validation (self-deactivation, exists check)
            User updatedUser = service.toggleActive(id, currentUser.getId());
            return ResponseEntity.ok(updatedUser);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}




