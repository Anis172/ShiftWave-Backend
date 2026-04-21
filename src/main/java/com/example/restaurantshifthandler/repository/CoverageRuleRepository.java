package com.example.restaurantshifthandler.repository;



import com.example.restaurantshifthandler.entity.CoverageRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CoverageRuleRepository extends JpaRepository<CoverageRule, Long> {
    List<CoverageRule> findByRestaurantId(Long restaurantId);
    Optional<CoverageRule> findByRestaurantIdAndRoleId(Long restaurantId, Long roleId);
}
