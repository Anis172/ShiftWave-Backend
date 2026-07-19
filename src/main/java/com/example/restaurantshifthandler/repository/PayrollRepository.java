package com.example.restaurantshifthandler.repository;

import com.example.restaurantshifthandler.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<PayrollRecord, Long> {

    List<PayrollRecord> findByRestaurantId(Long restaurantId);

    List<PayrollRecord> findByRestaurantIdAndMonthAndYear(
            Long restaurantId, Integer month, Integer year);

    Optional<PayrollRecord> findByWorkerIdAndMonthAndYear(
            Long workerId, Integer month, Integer year);

    List<PayrollRecord> findByWorkerIdAndYear(Long workerId, Integer year);

    @Query("SELECT pr FROM PayrollRecord pr " +
            "WHERE pr.restaurant.id = :restaurantId " +
            "AND pr.month = :month " +
            "AND pr.year = :year " +
            "AND (:workerName IS NULL OR LOWER(pr.worker.name) LIKE LOWER(CONCAT('%', :workerName, '%')))" +
            "ORDER BY pr.worker.name ASC")
    List<PayrollRecord> findByRestaurantIdAndMonthAndYearAndWorkerName(
            @Param("restaurantId") Long restaurantId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("workerName") String workerName);
}