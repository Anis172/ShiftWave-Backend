
package com.example.restaurantshifthandler.repository;

import com.example.restaurantshifthandler.entity.BreakRequest;
import com.example.restaurantshifthandler.entity.enums.BreakStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BreakRequestRepository extends JpaRepository<BreakRequest, Long> {
    List<BreakRequest> findByShiftId(Long shiftId);
    List<BreakRequest> findByWorkerId(Long workerId);
    List<BreakRequest> findByStatus(BreakStatus status);
    List<BreakRequest> findByStatusOrderByStartTimeDesc(BreakStatus status);

    @Query("SELECT br FROM BreakRequest br WHERE br.shift.restaurant.id = :restaurantId")
    List<BreakRequest> findByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("SELECT br FROM BreakRequest br WHERE br.shift.restaurant.id = :restaurantId AND br.status = :status ORDER BY br.startTime DESC")
    List<BreakRequest> findByRestaurantIdAndStatusOrderByStartTimeDesc(@Param("restaurantId") Long restaurantId, @Param("status") BreakStatus status);
}
