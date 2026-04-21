package com.example.restaurantshifthandler.controller;

import com.example.restaurantshifthandler.entity.Alert;
import com.example.restaurantshifthandler.entity.enums.AlertType;
import com.example.restaurantshifthandler.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService service;

    @GetMapping
    public List<Alert> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<Alert> getByRestaurant(@PathVariable Long restaurantId) {
        return service.findByRestaurant(restaurantId);
    }

    @GetMapping("/restaurant/{restaurantId}/unread")
    public List<Alert> getUnread(@PathVariable Long restaurantId) {
        return service.findUnreadByRestaurant(restaurantId);
    }

    @GetMapping("/type/{type}")
    public List<Alert> getByType(@PathVariable AlertType type) {
        return service.findByType(type);
    }

    @PostMapping
    public ResponseEntity<Alert> create(@RequestBody Alert alert) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(alert));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alert> update(@PathVariable Long id, @RequestBody Alert data) {
        return ResponseEntity.ok(service.update(id, data));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Alert> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(service.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
