package com.example.restaurantshifthandler.dto;

import com.example.restaurantshifthandler.entity.enums.BreakStatus;
import com.example.restaurantshifthandler.entity.enums.BreakType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakRequestDTO {

    private Long id;

    @NotNull(message = "Worker is required")
    private Long workerId;

    private String workerName;

    @NotNull(message = "Shift is required")
    private Long shiftId;

    @NotNull(message = "Break type is required")
    private BreakType breakType;

    private BreakStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
