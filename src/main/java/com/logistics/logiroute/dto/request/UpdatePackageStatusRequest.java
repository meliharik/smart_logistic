package com.logistics.logiroute.dto.request;

import com.logistics.logiroute.domain.enums.PackageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePackageStatusRequest {

    @NotNull(message = "Status is required")
    private PackageStatus status;
}
