package com.logistics.logiroute.service;

import com.logistics.logiroute.domain.entity.DeliveryRoute;
import com.logistics.logiroute.domain.entity.Package;
import com.logistics.logiroute.domain.entity.Vehicle;
import com.logistics.logiroute.domain.enums.PackageStatus;
import com.logistics.logiroute.domain.enums.VehicleStatus;
import com.logistics.logiroute.dto.DeliveryRouteDto;
import com.logistics.logiroute.dto.PackageDto;
import com.logistics.logiroute.exception.InvalidStatusTransitionException;
import com.logistics.logiroute.exception.ResourceNotFoundException;
import com.logistics.logiroute.exception.VehicleOverloadedException;
import com.logistics.logiroute.mapper.DeliveryRouteMapper;
import com.logistics.logiroute.mapper.PackageMapper;
import com.logistics.logiroute.repository.DeliveryRouteRepository;
import com.logistics.logiroute.repository.PackageRepository;
import com.logistics.logiroute.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final VehicleRepository vehicleRepository;
    private final PackageRepository packageRepository;
    private final DeliveryRouteRepository deliveryRouteRepository;
    private final PackageMapper packageMapper;
    private final DeliveryRouteMapper deliveryRouteMapper;

    /**
     * Core business logic: Assigns packages to a vehicle with capacity guard validation.
     * Implements smart routing by sorting packages by delivery deadline (earliest first).
     */
    @Transactional
    public DeliveryRouteDto assignPackagesToVehicle(Long vehicleId, List<Long> packageIds) {
        log.info("Attempting to assign {} packages to vehicle ID {}", packageIds.size(), vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> ResourceNotFoundException.forVehicle(vehicleId));

        List<Package> packages = packageRepository.findAllById(packageIds);

        if (packages.size() != packageIds.size()) {
            throw new IllegalArgumentException("Some package IDs were not found");
        }

        // CAPACITY GUARD: Calculate total weight and validate capacity
        double totalPackageWeight = packages.stream()
                .mapToDouble(Package::getWeightKg)
                .sum();

        if (!vehicle.canLoad(totalPackageWeight)) {
            log.error("Vehicle overload detected: Vehicle {} cannot load {} kg (remaining capacity: {} kg)",
                    vehicle.getLicensePlate(), totalPackageWeight, vehicle.getRemainingCapacityKg());
            throw VehicleOverloadedException.forPackage(
                    vehicle.getLicensePlate(),
                    totalPackageWeight,
                    vehicle.getRemainingCapacityKg()
            );
        }

        // SIMPLIFIED ROUTING: Sort packages by delivery deadline (earliest first)
        List<Package> sortedPackages = packages.stream()
                .sorted(Comparator.comparing(Package::getDeliveryDeadline))
                .toList();

        // Create delivery route
        DeliveryRoute deliveryRoute = DeliveryRoute.builder()
                .vehicle(vehicle)
                .createdAt(LocalDateTime.now())
                .build();

        // Add packages to route and update their status
        for (Package pkg : sortedPackages) {
            if (pkg.getStatus() != PackageStatus.CREATED) {
                throw new IllegalArgumentException(
                        String.format("Package ID %d is not in CREATED status", pkg.getId())
                );
            }

            deliveryRoute.addPackage(pkg);
            updatePackageStatus(pkg, PackageStatus.LOADED);
        }

        // Update vehicle load and status
        vehicle.addLoad(totalPackageWeight);
        vehicle.setStatus(VehicleStatus.IN_TRANSIT);

        // Persist changes
        deliveryRouteRepository.save(deliveryRoute);
        vehicleRepository.save(vehicle);

        log.info("Successfully assigned {} packages to vehicle {} (Route ID: {})",
                sortedPackages.size(), vehicle.getLicensePlate(), deliveryRoute.getId());

        return deliveryRouteMapper.toDto(deliveryRoute);
    }

    /**
     * Updates package status with state machine validation.
     * Ensures valid state transitions: CREATED -> LOADED -> DELIVERED
     */
    @Transactional
    public PackageDto updatePackageStatus(Long packageId, PackageStatus newStatus) {
        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> ResourceNotFoundException.forPackage(packageId));

        updatePackageStatus(pkg, newStatus);
        packageRepository.save(pkg);

        log.info("Package ID {} status updated: {} -> {}", packageId, pkg.getStatus(), newStatus);

        return packageMapper.toDto(pkg);
    }

    /**
     * Internal method to validate and update package status.
     * Implements state machine logic.
     */
    private void updatePackageStatus(Package pkg, PackageStatus newStatus) {
        if (!pkg.canTransitionTo(newStatus)) {
            log.error("Invalid status transition for package ID {}: {} -> {}",
                    pkg.getId(), pkg.getStatus(), newStatus);
            throw InvalidStatusTransitionException.forPackage(
                    pkg.getId(),
                    pkg.getStatus(),
                    newStatus
            );
        }

        pkg.setStatus(newStatus);
    }

    @Transactional(readOnly = true)
    public DeliveryRouteDto getDeliveryRoute(Long routeId) {
        DeliveryRoute route = deliveryRouteRepository.findById(routeId)
                .orElseThrow(() -> ResourceNotFoundException.forDeliveryRoute(routeId));

        return deliveryRouteMapper.toDto(route);
    }

    @Transactional(readOnly = true)
    public List<DeliveryRouteDto> getActiveRoutes() {
        List<DeliveryRoute> routes = deliveryRouteRepository.findByCompletedAtIsNull();
        return deliveryRouteMapper.toDtoList(routes);
    }

    @Transactional(readOnly = true)
    public List<DeliveryRouteDto> getRoutesByVehicle(Long vehicleId) {
        List<DeliveryRoute> routes = deliveryRouteRepository.findByVehicleId(vehicleId);
        return deliveryRouteMapper.toDtoList(routes);
    }

    @Transactional
    public DeliveryRouteDto completeRoute(Long routeId) {
        DeliveryRoute route = deliveryRouteRepository.findById(routeId)
                .orElseThrow(() -> ResourceNotFoundException.forDeliveryRoute(routeId));

        route.setCompletedAt(LocalDateTime.now());

        // Update vehicle status back to AVAILABLE and reduce load
        Vehicle vehicle = route.getVehicle();
        vehicle.removeLoad(route.getTotalWeight());
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        deliveryRouteRepository.save(route);
        vehicleRepository.save(vehicle);

        log.info("Route ID {} marked as completed for vehicle {}", routeId, vehicle.getLicensePlate());

        return deliveryRouteMapper.toDto(route);
    }
}
