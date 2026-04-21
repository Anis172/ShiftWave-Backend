package com.example.restaurantshifthandler.controller;

import com.example.restaurantshifthandler.dto.CoverageRuleDTO;
import com.example.restaurantshifthandler.entity.CoverageRule;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.service.CoverageRuleService;
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
@RequestMapping("/api/coverage-rules")
@RequiredArgsConstructor
public class CoverageRuleController {

    private final CoverageRuleService service;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<CoverageRule>> getAll(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();
        List<CoverageRule> rules = service.findByRestaurantId(restaurantId);

        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoverageRule> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<CoverageRule> getByRestaurant(@PathVariable Long restaurantId) {
        return service.findByRestaurant(restaurantId);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CoverageRuleDTO dto, BindingResult result, Authentication authentication) {

        // ✅ Check DTO validation errors
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        // ✅ Get current user's restaurant
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();

        // ✅ All good, create rule
        CoverageRule createdRule = service.save(dto, restaurantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CoverageRuleDTO dto, BindingResult result, Authentication authentication) {

        // ✅ Check DTO validation errors
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMessage));
        }

        // ✅ Check: Rule exists
        Optional<CoverageRule> ruleOpt = service.findById(id);
        if (ruleOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Coverage rule not found"));
        }

        // ✅ Get current user's restaurant
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();

        // ✅ All good, update rule
        CoverageRule updatedRule = service.update(id, dto, restaurantId);
        return ResponseEntity.ok(updatedRule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        // ✅ Check: Rule exists
        Optional<CoverageRule> ruleOpt = service.findById(id);
        if (ruleOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Coverage rule not found"));
        }

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

