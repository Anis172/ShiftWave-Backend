package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.entity.Alert;
import com.example.restaurantshifthandler.entity.enums.AlertType;
import com.example.restaurantshifthandler.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repository;

    public List<Alert> findAll() {
        return repository.findAll();
    }

    public Optional<Alert> findById(Long id) {
        return repository.findById(id);
    }

    public Alert save(Alert alert) {
        return repository.save(alert);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<Alert> findByRestaurant(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId);
    }

    public List<Alert> findUnreadByRestaurant(Long restaurantId) {
        return repository.findByRestaurantIdAndIsRead(restaurantId, false);
    }

    public List<Alert> findByType(AlertType type) {
        return repository.findByType(type);
    }

    public Alert markAsRead(Long id) {
        return repository.findById(id).map(a -> {
            a.setIsRead(true);
            return repository.save(a);
        }).orElseThrow(() -> new RuntimeException("Alert not found: " + id));
    }

    public Alert update(Long id, Alert data) {
        return repository.findById(id).map(a -> {
            a.setMessage(data.getMessage());
            a.setType(data.getType());
            a.setIsRead(data.getIsRead());
            return repository.save(a);
        }).orElseThrow(() -> new RuntimeException("Alert not found: " + id));
    }
}
