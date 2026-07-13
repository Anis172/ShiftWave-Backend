package com.example.restaurantshifthandler.controller;

import com.example.restaurantshifthandler.dto.RestaurantSignupDTO;
import com.example.restaurantshifthandler.entity.Restaurant;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.repository.RestaurantRepository;
import com.example.restaurantshifthandler.repository.RoleRepository;
import com.example.restaurantshifthandler.repository.UserRepository;
import com.example.restaurantshifthandler.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/public/register-restaurant")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RestaurantRegistrationController {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Transactional
    public ResponseEntity<?> registerRestaurant(
            @Valid @RequestBody RestaurantSignupDTO dto,
            BindingResult result,
            HttpServletResponse response) {

        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        if (userRepository.findByEmail(dto.getOwnerEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email already exists"));
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(dto.getRestaurantName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setPhone(dto.getPhone());
        restaurant.setCreatedAt(LocalDateTime.now());
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        User manager = new User();
        manager.setName(dto.getOwnerName());
        manager.setEmail(dto.getOwnerEmail());
        manager.setPassword(passwordEncoder.encode(dto.getPassword()));
        manager.setRole(roleRepository.findByName("Manager")
                .orElseThrow(() -> new RuntimeException("Manager role not found")));
        manager.setRestaurant(savedRestaurant);
        manager.setIsActive(true);
        manager.setCreatedAt(LocalDateTime.now());
        User savedManager = userRepository.save(manager);

        String token = jwtUtil.generateToken(savedManager.getEmail());

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 10);
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Restaurant registered successfully!",
                        "userId", savedManager.getId(),
                        "email", savedManager.getEmail(),
                        "name", savedManager.getName(),
                        "role", "Manager"
                ));
    }
}