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
import java.util.Optional;

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

        // ✅ Check password is provided (required for CREATE)
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password is required"));
        }

        // ✅ Check password length
        if (dto.getPassword().length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password must be at least 8 characters"));
        }

        // ✅ Check if email already exists
        if (service.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email already exists"));
        }

        // ✅ Get current user's restaurant
        String email = authentication.getName();
        User currentUser = service.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();

        // ✅ All good, create user
        User createdUser = service.save(dto, restaurantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto, BindingResult result, Authentication authentication) {

        // ✅ Check DTO validation errors
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        // ✅ Check if user exists
        Optional<User> existingUserOpt = service.findById(id);
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User not found"));
        }

        User existingUser = existingUserOpt.get();

        // ✅ Check password length IF password is being changed
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            if (dto.getPassword().length() < 8) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Password must be at least 8 characters"));
            }
        }

        // ✅ Check duplicate email ONLY if email changed
        if (!existingUser.getEmail().equals(dto.getEmail())) {
            // Email changed! Check if new email already exists
            if (service.existsByEmail(dto.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email already exists"));
            }
        }

        // ✅ Get current user's restaurant
        String email = authentication.getName();
        User currentUser = service.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();

        // ✅ All good, update user
        User updatedUser = service.update(id, dto, restaurantId);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {

        // ✅ Check if user exists
        Optional<User> userToDelete = service.findById(id);
        if (userToDelete.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User not found"));
        }

        String email = authentication.getName();
        User currentUser = service.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Prevent self-deletion
        if (currentUser.getId().equals(id)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You cannot delete your own account"));
        }

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, Authentication authentication) {

        // ✅ Check if user exists
        Optional<User> userOpt = service.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User not found"));
        }

        // ✅ Prevent self-deactivation
        String email = authentication.getName();
        User currentUser = service.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getId().equals(id)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You cannot deactivate your own account"));
        }

        User updatedUser = service.toggleActive(id);
        return ResponseEntity.ok(updatedUser);
    }


}




