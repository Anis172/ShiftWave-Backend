package com.example.restaurantshifthandler.controller;

import com.example.restaurantshifthandler.dto.LoginRequest;
import com.example.restaurantshifthandler.dto.LoginResponse;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.security.JwtUtil;
import com.example.restaurantshifthandler.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult result) {

        // ✅ Check DTO validation errors (email format, required fields)
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        // ✅ Check if user exists
        Optional<User> userOptional = userService.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid email or password"));
        }

        User user = userOptional.get();

        // ✅ Check if password matches
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid email or password"));
        }

        // ✅ Check if user is active
        if (!user.getIsActive()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Account is inactive. Please contact your manager."));
        }

        // ✅ All good! Generate token and return
        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(new LoginResponse(
                user.getId(),
                token,
                user.getEmail(),
                user.getName(),
                user.getRole().getName()
        ));
    }
}