package com.example.restaurantshifthandler.dto;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftDTO {

    private Long id;

    @NotNull(message = "Worker is required")
    private Long workerId;


    private Long restaurantId;

    @NotNull(message = "Role is required")
    private Long roleId;

    @NotNull(message = "Scheduled start time is required")
    private LocalDateTime scheduledStart;

    @NotNull(message = "Scheduled end time is required")
    private LocalDateTime scheduledEnd;

    private ShiftStatus status;
}
