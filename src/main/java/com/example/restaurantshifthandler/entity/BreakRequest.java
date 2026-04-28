package com.example.restaurantshifthandler.entity;

import com.example.restaurantshifthandler.entity.enums.BreakStatus;
import com.example.restaurantshifthandler.entity.enums.BreakType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "break_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Enumerated(EnumType.STRING)
    @Column(name = "break_type", nullable = false, length = 20)
    private BreakType breakType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BreakStatus status = BreakStatus.PENDING;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Column(name = "end_time")
    private LocalDateTime endTime;
}
