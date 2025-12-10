package com.logistics.logiroute.controller;

import com.logistics.logiroute.domain.enums.PackageStatus;
import com.logistics.logiroute.dto.PackageDto;
import com.logistics.logiroute.dto.request.UpdatePackageStatusRequest;
import com.logistics.logiroute.service.DeliveryService;
import com.logistics.logiroute.service.PackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;
    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<PackageDto> createPackage(@Valid @RequestBody PackageDto packageDto) {
        PackageDto created = packageService.createPackage(packageDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageDto> getPackage(@PathVariable Long id) {
        PackageDto pkg = packageService.getPackage(id);
        return ResponseEntity.ok(pkg);
    }

    @GetMapping
    public ResponseEntity<List<PackageDto>> getAllPackages() {
        List<PackageDto> packages = packageService.getAllPackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<PackageDto>> getUnassignedPackages() {
        List<PackageDto> packages = packageService.getUnassignedPackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PackageDto>> getPackagesByStatus(@PathVariable PackageStatus status) {
        List<PackageDto> packages = packageService.getPackagesByStatus(status);
        return ResponseEntity.ok(packages);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PackageDto> updatePackageStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePackageStatusRequest request) {
        PackageDto updated = deliveryService.updatePackageStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PackageDto> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody PackageDto packageDto) {
        PackageDto updated = packageService.updatePackage(id, packageDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}
