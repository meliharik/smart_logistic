package com.logistics.logiroute.config;

import com.logistics.logiroute.domain.entity.Package;
import com.logistics.logiroute.domain.entity.Vehicle;
import com.logistics.logiroute.domain.enums.PackageStatus;
import com.logistics.logiroute.domain.enums.VehicleStatus;
import com.logistics.logiroute.repository.PackageRepository;
import com.logistics.logiroute.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    @Bean
    CommandLineRunner loadData(VehicleRepository vehicleRepository, PackageRepository packageRepository) {
        return args -> {
            if (vehicleRepository.count() == 0 && packageRepository.count() == 0) {
                log.info("Loading seed data...");

                // Create 2 vehicles
                Vehicle vehicle1 = Vehicle.builder()
                        .licensePlate("ABC-1234")
                        .capacityKg(1000.0)
                        .currentLoadKg(0.0)
                        .status(VehicleStatus.AVAILABLE)
                        .build();

                Vehicle vehicle2 = Vehicle.builder()
                        .licensePlate("XYZ-5678")
                        .capacityKg(1500.0)
                        .currentLoadKg(0.0)
                        .status(VehicleStatus.AVAILABLE)
                        .build();

                vehicleRepository.save(vehicle1);
                vehicleRepository.save(vehicle2);

                log.info("Created 2 vehicles: {} ({}kg), {} ({}kg)",
                        vehicle1.getLicensePlate(), vehicle1.getCapacityKg(),
                        vehicle2.getLicensePlate(), vehicle2.getCapacityKg());

                // Create 5 packages with different weights and deadlines
                LocalDateTime now = LocalDateTime.now();

                Package package1 = Package.builder()
                        .deliveryAddress("123 Main St, New York, NY 10001")
                        .weightKg(150.0)
                        .status(PackageStatus.CREATED)
                        .deliveryDeadline(now.plusHours(4))
                        .build();

                Package package2 = Package.builder()
                        .deliveryAddress("456 Oak Ave, Los Angeles, CA 90001")
                        .weightKg(250.0)
                        .status(PackageStatus.CREATED)
                        .deliveryDeadline(now.plusHours(2))
                        .build();

                Package package3 = Package.builder()
                        .deliveryAddress("789 Pine Rd, Chicago, IL 60601")
                        .weightKg(300.0)
                        .status(PackageStatus.CREATED)
                        .deliveryDeadline(now.plusHours(6))
                        .build();

                Package package4 = Package.builder()
                        .deliveryAddress("321 Elm Blvd, Houston, TX 77001")
                        .weightKg(500.0)
                        .status(PackageStatus.CREATED)
                        .deliveryDeadline(now.plusHours(1))
                        .build();

                Package package5 = Package.builder()
                        .deliveryAddress("654 Maple Dr, Phoenix, AZ 85001")
                        .weightKg(200.0)
                        .status(PackageStatus.CREATED)
                        .deliveryDeadline(now.plusHours(3))
                        .build();

                packageRepository.save(package1);
                packageRepository.save(package2);
                packageRepository.save(package3);
                packageRepository.save(package4);
                packageRepository.save(package5);

                log.info("Created 5 packages:");
                log.info("  - Package 1: {}kg, deadline in 4 hours", package1.getWeightKg());
                log.info("  - Package 2: {}kg, deadline in 2 hours (urgent)", package2.getWeightKg());
                log.info("  - Package 3: {}kg, deadline in 6 hours", package3.getWeightKg());
                log.info("  - Package 4: {}kg, deadline in 1 hour (most urgent)", package4.getWeightKg());
                log.info("  - Package 5: {}kg, deadline in 3 hours", package5.getWeightKg());

                log.info("Seed data loaded successfully!");
                log.info("-----------------------------------------------------");
                log.info("TESTING SCENARIOS:");
                log.info("1. Assign packages 4,2,5 to vehicle ABC-1234 (total 950kg < 1000kg) - SHOULD SUCCEED");
                log.info("   - Packages will be sorted by deadline: package 4 (1h), package 2 (2h), package 5 (3h)");
                log.info("2. Try to assign package 3 (300kg) to ABC-1234 after above - SHOULD FAIL (overload)");
                log.info("3. Assign packages 1,3 to vehicle XYZ-5678 (total 450kg < 1500kg) - SHOULD SUCCEED");
                log.info("-----------------------------------------------------");
            } else {
                log.info("Data already exists, skipping seed data loading");
            }
        };
    }
}
