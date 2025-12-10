package com.logistics.logiroute.dto;

import com.logistics.logiroute.domain.enums.PackageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageDto {

    private Long id;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weightKg;

    @NotNull(message = "Status is required")
    private PackageStatus status;

    @NotNull(message = "Delivery deadline is required")
    private LocalDateTime deliveryDeadline;

    private Long deliveryRouteId;
}
