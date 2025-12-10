package com.logistics.logiroute.repository;

import com.logistics.logiroute.domain.entity.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, Long> {

    List<DeliveryRoute> findByVehicleId(Long vehicleId);

    List<DeliveryRoute> findByCompletedAtIsNull();

    List<DeliveryRoute> findByCompletedAtIsNotNull();
}
