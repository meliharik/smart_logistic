package com.logistics.logiroute.controller;

import com.logistics.logiroute.dto.VehicleDto;
import com.logistics.logiroute.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleDto> createVehicle(@Valid @RequestBody VehicleDto vehicleDto) {
        VehicleDto created = vehicleService.createVehicle(vehicleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getVehicle(@PathVariable Long id) {
        VehicleDto vehicle = vehicleService.getVehicle(id);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping
    public ResponseEntity<List<VehicleDto>> getAllVehicles() {
        List<VehicleDto> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/available")
    public ResponseEntity<List<VehicleDto>> getAvailableVehicles() {
        List<VehicleDto> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleDto> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleDto vehicleDto) {
        VehicleDto updated = vehicleService.updateVehicle(id, vehicleDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
