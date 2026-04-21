package com.example.restaurantshifthandler.repository;


import com.example.restaurantshifthandler.entity.Alert;
import com.example.restaurantshifthandler.entity.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByRestaurantId(Long restaurantId);
    List<Alert> findByRestaurantIdAndIsRead(Long restaurantId, Boolean isRead);
    List<Alert> findByType(AlertType type);
}