package com.example.restaurantshifthandler.repository;


import com.example.restaurantshifthandler.entity.Shift;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByWorkerId(Long workerId);
    List<Shift> findByRestaurantId(Long restaurantId);
    List<Shift> findByStatus(ShiftStatus status);
    int countByRoleIdAndStatus(Long roleId, ShiftStatus status);
    @Query("SELECT s FROM Shift s " +
            "WHERE s.restaurant.id = :restaurantId " +
            "AND (:workerName IS NULL OR LOWER(s.worker.name) LIKE LOWER(CONCAT('%', :workerName, '%'))) " +
            "AND (:roleId IS NULL OR s.role.id = :roleId) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (:startDate IS NULL OR s.scheduledStart >= :startDate) " +
            "AND (:endDate IS NULL OR s.scheduledStart <= :endDate) " +
            "ORDER BY s.scheduledStart DESC")
    Page<Shift> findShiftsPaginated(
            @Param("restaurantId") Long restaurantId,
            @Param("workerName") String workerName,
            @Param("roleId") Long roleId,
            @Param("status") ShiftStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
