
package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.dto.UserDTO;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.repository.RestaurantRepository;
import com.example.restaurantshifthandler.repository.RoleRepository;
import com.example.restaurantshifthandler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public List<User> findByRestaurant(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId);
    }

    public List<User> findByRestaurantId(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId);
    }

    public User save(UserDTO dto, Long restaurantId) {
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(roleRepository.findById(dto.getRoleId())
                        .orElseThrow(() -> new RuntimeException("Role not found")))
                .restaurant(restaurantRepository.findById(restaurantId)
                        .orElseThrow(() -> new RuntimeException("Restaurant not found")))
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        return repository.save(user);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public User update(Long id, UserDTO dto, Long restaurantId) {
        return repository.findById(id).map(user -> {
            user.setName(dto.getName());
            user.setEmail(dto.getEmail());
            user.setRole(roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found")));
            user.setRestaurant(restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found")));
            user.setIsActive(dto.getIsActive());

            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            return repository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public User toggleActive(Long id) {
        return repository.findById(id).map(user -> {
            user.setIsActive(!user.getIsActive());
            return repository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    // ✅ NEW: Check if email already exists
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}