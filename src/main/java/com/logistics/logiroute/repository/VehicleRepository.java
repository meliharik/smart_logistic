package com.logistics.logiroute.repository;

import com.logistics.logiroute.domain.entity.Vehicle;
import com.logistics.logiroute.domain.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByStatus(VehicleStatus status);

    List<Vehicle> findByStatusAndCapacityKgGreaterThanEqual(VehicleStatus status, Double minCapacity);
}
