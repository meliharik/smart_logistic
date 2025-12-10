package com.logistics.logiroute.controller;

import com.logistics.logiroute.dto.DeliveryRouteDto;
import com.logistics.logiroute.dto.request.AssignPackagesRequest;
import com.logistics.logiroute.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/assign")
    public ResponseEntity<DeliveryRouteDto> assignPackagesToVehicle(
            @Valid @RequestBody AssignPackagesRequest request) {
        DeliveryRouteDto route = deliveryService.assignPackagesToVehicle(
                request.getVehicleId(),
                request.getPackageIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(route);
    }

    @GetMapping("/routes/{id}")
    public ResponseEntity<DeliveryRouteDto> getDeliveryRoute(@PathVariable Long id) {
        DeliveryRouteDto route = deliveryService.getDeliveryRoute(id);
        return ResponseEntity.ok(route);
    }

    @GetMapping("/routes")
    public ResponseEntity<List<DeliveryRouteDto>> getActiveRoutes() {
        List<DeliveryRouteDto> routes = deliveryService.getActiveRoutes();
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/routes/vehicle/{vehicleId}")
    public ResponseEntity<List<DeliveryRouteDto>> getRoutesByVehicle(@PathVariable Long vehicleId) {
        List<DeliveryRouteDto> routes = deliveryService.getRoutesByVehicle(vehicleId);
        return ResponseEntity.ok(routes);
    }

    @PatchMapping("/routes/{id}/complete")
    public ResponseEntity<DeliveryRouteDto> completeRoute(@PathVariable Long id) {
        DeliveryRouteDto route = deliveryService.completeRoute(id);
        return ResponseEntity.ok(route);
    }
}
