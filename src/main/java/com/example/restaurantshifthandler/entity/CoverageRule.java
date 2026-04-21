package com.example.restaurantshifthandler.entity;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coverage_rule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "minimum_workers", nullable = false)
    private Integer minimumWorkers = 1;
}
