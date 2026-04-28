package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.dto.ShiftDTO;
import com.example.restaurantshifthandler.entity.Shift;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import com.example.restaurantshifthandler.repository.ShiftRepository;
import com.example.restaurantshifthandler.repository.UserRepository;
import com.example.restaurantshifthandler.repository.RoleRepository;
import com.example.restaurantshifthandler.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository repository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;

    public List<Shift> findAll() {
        return repository.findAll();
    }

    public Optional<Shift> findById(Long id) {
        return repository.findById(id);
    }

    public List<Shift> findByRestaurantId(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId);
    }

    public Shift save(ShiftDTO dto, Long restaurantId) {
        Shift shift = Shift.builder()
                .worker(userRepository.findById(dto.getWorkerId())
                        .orElseThrow(() -> new RuntimeException("Worker not found")))
                .role(roleRepository.findById(dto.getRoleId())
                        .orElseThrow(() -> new RuntimeException("Role not found")))
                .restaurant(restaurantRepository.findById(restaurantId)
                        .orElseThrow(() -> new RuntimeException("Restaurant not found")))
                .scheduledStart(dto.getScheduledStart())
                .scheduledEnd(dto.getScheduledEnd())
                .status(dto.getStatus() != null ? dto.getStatus() : ShiftStatus.SCHEDULED)
                .build();

        return repository.save(shift);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public Shift update(Long id, ShiftDTO dto, Long restaurantId) {
        return repository.findById(id).map(shift -> {
            shift.setWorker(userRepository.findById(dto.getWorkerId())
                    .orElseThrow(() -> new RuntimeException("Worker not found")));
            shift.setRole(roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found")));
            shift.setRestaurant(restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found")));
            shift.setScheduledStart(dto.getScheduledStart());
            shift.setScheduledEnd(dto.getScheduledEnd());

            if (dto.getScheduledEnd().isAfter(LocalDateTime.now())) {
                shift.setStatus(ShiftStatus.SCHEDULED);
                shift.setClockInTime(null);
                shift.setClockOutTime(null);
            } else if (dto.getStatus() != null) {
                shift.setStatus(dto.getStatus());
            }

            return repository.save(shift);
        }).orElseThrow(() -> new RuntimeException("Shift not found: " + id));
    }

public Shift clockIn(Long id) {
    return repository.findById(id).map(shift -> {
        shift.setClockInTime(LocalDateTime.now());
        shift.setStatus(ShiftStatus.ACTIVE);
        return repository.save(shift);
    }).orElseThrow(() -> new RuntimeException("Shift not found: " + id));
}
    public Shift clockOut(Long id) {
        return repository.findById(id).map(shift -> {
            shift.setClockOutTime(LocalDateTime.now());
            shift.setStatus(ShiftStatus.COMPLETED);
            return repository.save(shift);
        }).orElseThrow(() -> new RuntimeException("Shift not found: " + id));
    }

    public List<Shift> findByWorker(Long workerId) {
        return repository.findByWorkerId(workerId);
    }

    public List<Shift> findByRestaurant(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId);
    }

    public List<Shift> findByStatus(ShiftStatus status) {
        return repository.findByStatus(status);
    }

    public Shift cancel(Long id) {
        return repository.findById(id).map(shift -> {
            shift.setStatus(ShiftStatus.CANCELLED);
            return repository.save(shift);
        }).orElseThrow(() -> new RuntimeException("Shift not found: " + id));
    }

    public void updateExpiredShifts() {
        LocalDateTime now = LocalDateTime.now();

        List<Shift> scheduledShifts = repository.findByStatus(ShiftStatus.SCHEDULED);
        for (Shift shift : scheduledShifts) {
            if (shift.getScheduledEnd().isBefore(now)) {
                shift.setStatus(ShiftStatus.MISSED);
                repository.save(shift);
            }
        }

        List<Shift> activeShifts = repository.findByStatus(ShiftStatus.ACTIVE);
        for (Shift shift : activeShifts) {
            if (shift.getScheduledEnd().isBefore(now)) {
                shift.setClockOutTime(shift.getScheduledEnd());
                shift.setStatus(ShiftStatus.COMPLETED);
                repository.save(shift);
            }
        }
    }
}
