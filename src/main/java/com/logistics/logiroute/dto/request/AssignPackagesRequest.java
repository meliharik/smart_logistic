package com.logistics.logiroute.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignPackagesRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotEmpty(message = "Package IDs list cannot be empty")
    private List<Long> packageIds;
}
