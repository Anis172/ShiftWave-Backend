package com.example.restaurantshifthandler.entity;

import com.example.restaurantshifthandler.entity.enums.SalaryType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "app_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ✅ Salary fields (all nullable - not mandatory!)
    @Enumerated(EnumType.STRING)
    @Column(name = "salary_type", nullable = true)
    private SalaryType salaryType;

    @Column(name = "hourly_rate", nullable = true)
    private Double hourlyRate;

    @Column(name = "daily_rate", nullable = true)
    private Double dailyRate;

    @Column(name = "monthly_salary", nullable = true)
    private Double monthlySalary;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}