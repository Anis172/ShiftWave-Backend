package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.dto.UserDTO;
import com.example.restaurantshifthandler.entity.Role;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.Restaurant;
import com.example.restaurantshifthandler.exception.ResourceNotFoundException;
import com.example.restaurantshifthandler.repository.RoleRepository;
import com.example.restaurantshifthandler.repository.UserRepository;
import com.example.restaurantshifthandler.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<User> findByRestaurantId(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId);
    }

    public User save(UserDTO dto, Long restaurantId) {
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .restaurant(restaurant)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .salaryType(dto.getSalaryType())
                .hourlyRate(dto.getHourlyRate())
                .dailyRate(dto.getDailyRate())
                .monthlySalary(dto.getMonthlySalary())
                .build();

        return repository.save(user);
    }

    public User update(Long id, UserDTO dto, Long currentUserId) {

        if (id.equals(currentUserId)) {
            throw new RuntimeException("You cannot edit your own account");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(role);

        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        user.setSalaryType(dto.getSalaryType());
        user.setHourlyRate(dto.getHourlyRate());
        user.setDailyRate(dto.getDailyRate());
        user.setMonthlySalary(dto.getMonthlySalary());

        return repository.save(user);
    }

    public void deleteById(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new RuntimeException("You cannot delete your own account");
        }

        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        repository.delete(user);
    }

    public User toggleActive(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new RuntimeException("You cannot deactivate your own account");
        }

        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        user.setIsActive(!user.getIsActive());
        return repository.save(user);
    }
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}