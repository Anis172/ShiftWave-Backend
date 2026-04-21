package com.example.restaurantshifthandler.dto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageRuleDTO {

    private Long id;

    @NotNull(message = "Role is required")
    private Long roleId;


    private Long restaurantId;

    @NotNull(message = "Minimum workers is required")
    @Min(value = 1, message = "Minimum workers must be at least 1")
    private Integer minimumWorkers;
}
