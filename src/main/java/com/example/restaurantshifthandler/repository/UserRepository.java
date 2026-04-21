package com.example.restaurantshifthandler.repository;

import com.example.restaurantshifthandler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRestaurantId(Long restaurantId);
    List<User> findByIsActive(Boolean isActive);
    boolean existsByEmail(String email);
}