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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryService Unit Tests")
class DeliveryServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private DeliveryRouteRepository deliveryRouteRepository;

    @Mock
    private PackageMapper packageMapper;

    @Mock
    private DeliveryRouteMapper deliveryRouteMapper;

    @InjectMocks
    private DeliveryService deliveryService;

    private Vehicle testVehicle;
    private Package package1;
    private Package package2;
    private Package package3;

    @BeforeEach
    void setUp() {
        testVehicle = Vehicle.builder()
                .id(1L)
                .licensePlate("TEST-123")
                .capacityKg(1000.0)
                .currentLoadKg(0.0)
                .status(VehicleStatus.AVAILABLE)
                .build();

        LocalDateTime now = LocalDateTime.now();

        package1 = Package.builder()
                .id(1L)
                .deliveryAddress("Address 1")
                .weightKg(200.0)
                .status(PackageStatus.CREATED)
                .deliveryDeadline(now.plusHours(2))
                .build();

        package2 = Package.builder()
                .id(2L)
                .deliveryAddress("Address 2")
                .weightKg(300.0)
                .status(PackageStatus.CREATED)
                .deliveryDeadline(now.plusHours(1))
                .build();

        package3 = Package.builder()
                .id(3L)
                .deliveryAddress("Address 3")
                .weightKg(500.0)
                .status(PackageStatus.CREATED)
                .deliveryDeadline(now.plusHours(3))
                .build();
    }

    @Test
    @DisplayName("Should successfully assign packages when total weight is within capacity")
    void assignPackagesToVehicle_Success() {
        List<Long> packageIds = Arrays.asList(1L, 2L);
        List<Package> packages = Arrays.asList(package1, package2);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(packageRepository.findAllById(packageIds)).thenReturn(packages);
        when(deliveryRouteRepository.save(any(DeliveryRoute.class))).thenAnswer(i -> i.getArgument(0));
        when(deliveryRouteMapper.toDto(any(DeliveryRoute.class))).thenReturn(new DeliveryRouteDto());

        DeliveryRouteDto result = deliveryService.assignPackagesToVehicle(1L, packageIds);

        assertThat(result).isNotNull();
        verify(vehicleRepository).save(testVehicle);
        verify(deliveryRouteRepository).save(any(DeliveryRoute.class));

        assertThat(testVehicle.getCurrentLoadKg()).isEqualTo(500.0);
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.IN_TRANSIT);
        assertThat(package1.getStatus()).isEqualTo(PackageStatus.LOADED);
        assertThat(package2.getStatus()).isEqualTo(PackageStatus.LOADED);
    }

    @Test
    @DisplayName("CAPACITY GUARD: Should throw VehicleOverloadedException when package weight exceeds remaining capacity")
    void assignPackagesToVehicle_CapacityGuard_ThrowsException() {
        testVehicle.setCurrentLoadKg(900.0);

        List<Long> packageIds = Arrays.asList(3L);
        List<Package> packages = Arrays.asList(package3);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(packageRepository.findAllById(packageIds)).thenReturn(packages);

        assertThatThrownBy(() -> deliveryService.assignPackagesToVehicle(1L, packageIds))
                .isInstanceOf(VehicleOverloadedException.class)
                .hasMessageContaining("TEST-123")
                .hasMessageContaining("500")
                .hasMessageContaining("100");

        verify(deliveryRouteRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("CAPACITY GUARD: Should throw exception when multiple packages exceed capacity")
    void assignPackagesToVehicle_MultiplePackages_ExceedsCapacity() {
        package1.setWeightKg(400.0);
        package2.setWeightKg(350.0);
        package3.setWeightKg(300.0);

        List<Long> packageIds = Arrays.asList(1L, 2L, 3L);
        List<Package> packages = Arrays.asList(package1, package2, package3);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(packageRepository.findAllById(packageIds)).thenReturn(packages);

        assertThatThrownBy(() -> deliveryService.assignPackagesToVehicle(1L, packageIds))
                .isInstanceOf(VehicleOverloadedException.class)
                .hasMessageContaining("1050");

        verify(deliveryRouteRepository, never()).save(any());
    }

    @Test
    @DisplayName("SMART ROUTING: Should sort packages by delivery deadline (earliest first)")
    void assignPackagesToVehicle_SortsByDeadline() {
        List<Long> packageIds = Arrays.asList(1L, 2L);
        List<Package> packages = Arrays.asList(package1, package2);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(packageRepository.findAllById(packageIds)).thenReturn(packages);
        when(deliveryRouteRepository.save(any(DeliveryRoute.class))).thenAnswer(i -> i.getArgument(0));
        when(deliveryRouteMapper.toDto(any(DeliveryRoute.class))).thenReturn(new DeliveryRouteDto());

        deliveryService.assignPackagesToVehicle(1L, packageIds);

        ArgumentCaptor<DeliveryRoute> routeCaptor = ArgumentCaptor.forClass(DeliveryRoute.class);
        verify(deliveryRouteRepository).save(routeCaptor.capture());

        DeliveryRoute savedRoute = routeCaptor.getValue();
        List<Package> sortedPackages = savedRoute.getPackages();

        assertThat(sortedPackages).hasSize(2);
        assertThat(sortedPackages.get(0).getId()).isEqualTo(2L);
        assertThat(sortedPackages.get(1).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when vehicle not found")
    void assignPackagesToVehicle_VehicleNotFound() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryService.assignPackagesToVehicle(999L, Arrays.asList(1L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("STATE MACHINE: Should allow CREATED -> LOADED transition")
    void updatePackageStatus_ValidTransition_CreatedToLoaded() {
        Package pkg = Package.builder()
                .id(1L)
                .deliveryAddress("Address")
                .weightKg(100.0)
                .status(PackageStatus.CREATED)
                .deliveryDeadline(LocalDateTime.now().plusHours(1))
                .build();

        when(packageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        when(packageRepository.save(any(Package.class))).thenReturn(pkg);
        when(packageMapper.toDto(any(Package.class))).thenReturn(new PackageDto());

        deliveryService.updatePackageStatus(1L, PackageStatus.LOADED);

        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.LOADED);
        verify(packageRepository).save(pkg);
    }

    @Test
    @DisplayName("STATE MACHINE: Should allow LOADED -> DELIVERED transition")
    void updatePackageStatus_ValidTransition_LoadedToDelivered() {
        Package pkg = Package.builder()
                .id(1L)
                .deliveryAddress("Address")
                .weightKg(100.0)
                .status(PackageStatus.LOADED)
                .deliveryDeadline(LocalDateTime.now().plusHours(1))
                .build();

        when(packageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        when(packageRepository.save(any(Package.class))).thenReturn(pkg);
        when(packageMapper.toDto(any(Package.class))).thenReturn(new PackageDto());

        deliveryService.updatePackageStatus(1L, PackageStatus.DELIVERED);

        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.DELIVERED);
        verify(packageRepository).save(pkg);
    }

    @Test
    @DisplayName("STATE MACHINE: Should prevent CREATED -> DELIVERED transition")
    void updatePackageStatus_InvalidTransition_CreatedToDelivered() {
        Package pkg = Package.builder()
                .id(1L)
                .deliveryAddress("Address")
                .weightKg(100.0)
                .status(PackageStatus.CREATED)
                .deliveryDeadline(LocalDateTime.now().plusHours(1))
                .build();

        when(packageRepository.findById(1L)).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> deliveryService.updatePackageStatus(1L, PackageStatus.DELIVERED))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("CREATED")
                .hasMessageContaining("DELIVERED");

        verify(packageRepository, never()).save(any());
    }

    @Test
    @DisplayName("STATE MACHINE: Should prevent backward transition LOADED -> CREATED")
    void updatePackageStatus_InvalidTransition_LoadedToCreated() {
        Package pkg = Package.builder()
                .id(1L)
                .deliveryAddress("Address")
                .weightKg(100.0)
                .status(PackageStatus.LOADED)
                .deliveryDeadline(LocalDateTime.now().plusHours(1))
                .build();

        when(packageRepository.findById(1L)).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> deliveryService.updatePackageStatus(1L, PackageStatus.CREATED))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("LOADED")
                .hasMessageContaining("CREATED");
    }

    @Test
    @DisplayName("STATE MACHINE: Should prevent any transition from DELIVERED status")
    void updatePackageStatus_InvalidTransition_FromDelivered() {
        Package pkg = Package.builder()
                .id(1L)
                .deliveryAddress("Address")
                .weightKg(100.0)
                .status(PackageStatus.DELIVERED)
                .deliveryDeadline(LocalDateTime.now().plusHours(1))
                .build();

        when(packageRepository.findById(1L)).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> deliveryService.updatePackageStatus(1L, PackageStatus.LOADED))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("DELIVERED");
    }

    @Test
    @DisplayName("Should complete route and update vehicle status")
    void completeRoute_Success() {
        DeliveryRoute route = DeliveryRoute.builder()
                .id(1L)
                .vehicle(testVehicle)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        route.addPackage(package1);
        route.addPackage(package2);

        testVehicle.setCurrentLoadKg(500.0);
        testVehicle.setStatus(VehicleStatus.IN_TRANSIT);

        when(deliveryRouteRepository.findById(1L)).thenReturn(Optional.of(route));
        when(deliveryRouteRepository.save(any(DeliveryRoute.class))).thenReturn(route);
        when(deliveryRouteMapper.toDto(any(DeliveryRoute.class))).thenReturn(new DeliveryRouteDto());

        deliveryService.completeRoute(1L);

        assertThat(route.getCompletedAt()).isNotNull();
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(testVehicle.getCurrentLoadKg()).isEqualTo(0.0);
        verify(vehicleRepository).save(testVehicle);
    }

    @Test
    @DisplayName("Should throw exception when trying to assign non-CREATED package")
    void assignPackagesToVehicle_PackageNotInCreatedStatus() {
        package1.setStatus(PackageStatus.LOADED);

        List<Long> packageIds = Arrays.asList(1L);
        List<Package> packages = Arrays.asList(package1);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(packageRepository.findAllById(packageIds)).thenReturn(packages);

        assertThatThrownBy(() -> deliveryService.assignPackagesToVehicle(1L, packageIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not in CREATED status");
    }
}
