package com.logistics.logiroute.dto;

import com.logistics.logiroute.domain.enums.VehicleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {

    private Long id;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Capacity is required")
    @PositiveOrZero(message = "Capacity must be positive or zero")
    private Double capacityKg;

    @PositiveOrZero(message = "Current load must be positive or zero")
    private Double currentLoadKg;

    @NotNull(message = "Status is required")
    private VehicleStatus status;

    private Double remainingCapacityKg;
}
