package com.example.restaurantshifthandler.dto;

import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime scheduledStart;

    @NotNull(message = "Scheduled end time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime scheduledEnd;

    private ShiftStatus status;
}
