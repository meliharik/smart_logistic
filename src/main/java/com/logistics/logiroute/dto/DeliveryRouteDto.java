package com.logistics.logiroute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRouteDto {

    private Long id;
    private Long vehicleId;
    private String vehicleLicensePlate;
    private List<PackageDto> packages;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Double totalWeight;
}
