package com.example.restaurantshifthandler.repository;


import com.example.restaurantshifthandler.entity.Shift;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByWorkerId(Long workerId);
    List<Shift> findByRestaurantId(Long restaurantId);
    List<Shift> findByStatus(ShiftStatus status);
    int countByRoleIdAndStatus(Long roleId, ShiftStatus status);
}
