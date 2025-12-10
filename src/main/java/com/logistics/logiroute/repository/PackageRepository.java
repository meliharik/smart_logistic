package com.logistics.logiroute.repository;

import com.logistics.logiroute.domain.entity.Package;
import com.logistics.logiroute.domain.enums.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    List<Package> findByStatus(PackageStatus status);

    List<Package> findByStatusAndDeliveryDeadlineBefore(PackageStatus status, LocalDateTime deadline);

    List<Package> findByDeliveryRouteId(Long deliveryRouteId);

    List<Package> findByDeliveryRouteIsNull();
}
