package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.entity.Alert;
import com.example.restaurantshifthandler.entity.BreakRequest;
import com.example.restaurantshifthandler.entity.CoverageRule;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.enums.AlertType;
import com.example.restaurantshifthandler.entity.enums.BreakStatus;
import com.example.restaurantshifthandler.entity.enums.BreakType;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import com.example.restaurantshifthandler.repository.BreakRequestRepository;
import com.example.restaurantshifthandler.repository.CoverageRuleRepository;
import com.example.restaurantshifthandler.repository.ShiftRepository;
import com.example.restaurantshifthandler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BreakRequestService {

    private final BreakRequestRepository repository;
    private final ShiftRepository shiftRepository;
    private final CoverageRuleRepository coverageRuleRepository;
    private final AlertService alertService;
    private final UserRepository userRepository;

    public List<BreakRequest> findAll() {
        return repository.findAll();
    }

    public Optional<BreakRequest> findById(Long id) {
        return repository.findById(id);
    }

    public BreakRequest save(BreakRequest breakRequest) {
        User fullWorker = userRepository.findById(breakRequest.getWorker().getId())
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        breakRequest.setWorker(fullWorker);

        // ✅ Check if worker already has an active/pending/approved break
        List<BreakRequest> existingBreaks = repository.findByWorkerId(fullWorker.getId());

        for (BreakRequest existing : existingBreaks) {
            if (existing.getStatus() == BreakStatus.PENDING) {
                throw new RuntimeException("You already have a pending break request");
            }
            if (existing.getStatus() == BreakStatus.ACTIVE) {
                throw new RuntimeException("You are already on a break");
            }
            if (existing.getStatus() == BreakStatus.APPROVED) {
                throw new RuntimeException("You already have an approved break waiting to start");
            }
        }

        // ✅ Get role from SHIFT, not from USER
        Long shiftRoleId = breakRequest.getShift().getRole().getId();
        Long restaurantId = breakRequest.getShift().getRestaurant().getId();

        // Count active workers with THIS SHIFT ROLE
        int activeWorkers = shiftRepository
                .countByRoleIdAndStatus(shiftRoleId, ShiftStatus.ACTIVE);

        // Get coverage rule for THIS SHIFT ROLE
        CoverageRule rule = coverageRuleRepository
                .findByRestaurantIdAndRoleId(restaurantId, shiftRoleId)
                .orElse(null);

        if (rule != null && activeWorkers - 1 >= rule.getMinimumWorkers()) {
            breakRequest.setStatus(BreakStatus.APPROVED);
            breakRequest.setStartTime(LocalDateTime.now());
            int durationMinutes = getBreakDuration(breakRequest.getBreakType());
            breakRequest.setEndTime(LocalDateTime.now().plusMinutes(durationMinutes));
        } else {
            breakRequest.setStatus(BreakStatus.DENIED);

            Alert alert = Alert.builder()
                    .restaurant(fullWorker.getRestaurant())
                    .type(AlertType.BREAK_DENIED)
                    .message("Break denied for " + fullWorker.getName()
                            + " - insufficient coverage for "
                            + breakRequest.getShift().getRole().getName())
                    .isRead(false)
                    .build();

            alertService.save(alert);
        }

        return repository.save(breakRequest);
    }
    public void deleteById(Long id) {
        BreakRequest breakRequest = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Break request not found: " + id));

        if (breakRequest.getStatus() == BreakStatus.ACTIVE||breakRequest.getStatus()==BreakStatus.COMPLETED) {
            throw new RuntimeException("Can only delete break requests with PENDING status!");
        }

        repository.deleteById(id);
    }

    public List<BreakRequest> findByShift(Long shiftId) {
        return repository.findByShiftId(shiftId);
    }

    public List<BreakRequest> findByWorker(Long workerId) {
        return repository.findByWorkerId(workerId);
    }

    public List<BreakRequest> findByStatus(BreakStatus status) {
        return repository.findByStatus(status);
    }

    public BreakRequest update(Long id, BreakRequest data) {
        return repository.findById(id).map(breakRequest -> {

            if (breakRequest.getStatus() != BreakStatus.PENDING) {
                throw new RuntimeException("Can only edit break requests with PENDING status!");
            }

            breakRequest.setBreakType(data.getBreakType());
            breakRequest.setStartTime(data.getStartTime());
            breakRequest.setEndTime(data.getEndTime());

            return repository.save(breakRequest);
        }).orElseThrow(() -> new RuntimeException("Break request not found: " + id));
    }

    public BreakRequest approve(Long id) {
        return repository.findById(id).map(breakRequest -> {
            breakRequest.setStatus(BreakStatus.APPROVED);
            return repository.save(breakRequest);
        }).orElseThrow(() -> new RuntimeException("BreakRequest not found: " + id));
    }

    public BreakRequest deny(Long id) {
        return repository.findById(id).map(breakRequest -> {
            breakRequest.setStatus(BreakStatus.DENIED);
            return repository.save(breakRequest);
        }).orElseThrow(() -> new RuntimeException("BreakRequest not found: " + id));
    }

    public BreakRequest start(Long id) {
        return repository.findById(id).map(breakRequest -> {
            breakRequest.setStartTime(LocalDateTime.now());
            breakRequest.setStatus(BreakStatus.ACTIVE);
            return repository.save(breakRequest);
        }).orElseThrow(() -> new RuntimeException("BreakRequest not found: " + id));
    }

    public BreakRequest complete(Long id) {
        return repository.findById(id).map(breakRequest -> {
            breakRequest.setEndTime(LocalDateTime.now());
            breakRequest.setStatus(BreakStatus.COMPLETED);
            return repository.save(breakRequest);
        }).orElseThrow(() -> new RuntimeException("BreakRequest not found: " + id));
    }

    private int getBreakDuration(BreakType breakType) {
        switch (breakType) {
            case LUNCH:
                return 30;
            case SHORT:
                return 15;
            case EMERGENCY:
                return 60;
            case SICK_LEAVE:
                return 480;
            case PERSONAL:
                return 30;
            default:
                return 15;
        }
    }

    public void updateExpiredBreaks() {
        LocalDateTime now = LocalDateTime.now();

        List<BreakRequest> activeBreaks = repository.findByStatus(BreakStatus.ACTIVE);
        for (BreakRequest breakRequest : activeBreaks) {
            if (breakRequest.getEndTime() != null && breakRequest.getEndTime().isBefore(now)) {
                breakRequest.setStatus(BreakStatus.COMPLETED);
                repository.save(breakRequest);
            }
        }

        LocalDateTime yesterday = now.minusHours(24);
        List<BreakRequest> pendingBreaks = repository.findByStatus(BreakStatus.PENDING);
        for (BreakRequest breakRequest : pendingBreaks) {
            if (breakRequest.getRequestedAt().isBefore(yesterday)) {
                breakRequest.setStatus(BreakStatus.DENIED);
                repository.save(breakRequest);
            }
        }
    }

    public List<BreakRequest> findByStatusOrderByStartTimeDesc(BreakStatus status) {
        return repository.findByStatusOrderByStartTimeDesc(status);
    }

    public List<BreakRequest> findByRestaurantId(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId);
    }

    public List<BreakRequest> findByRestaurantIdAndStatusOrderByStartTimeDesc(Long restaurantId, BreakStatus status) {
        return repository.findByRestaurantIdAndStatusOrderByStartTimeDesc(restaurantId, status);
    }
}
