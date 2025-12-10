package com.logistics.logiroute.service;

import com.logistics.logiroute.domain.entity.Vehicle;
import com.logistics.logiroute.domain.enums.VehicleStatus;
import com.logistics.logiroute.dto.VehicleDto;
import com.logistics.logiroute.exception.ResourceNotFoundException;
import com.logistics.logiroute.mapper.VehicleMapper;
import com.logistics.logiroute.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public VehicleDto createVehicle(VehicleDto vehicleDto) {
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDto);
        vehicle.setCurrentLoadKg(0.0);
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Created vehicle: {}", saved.getLicensePlate());

        return vehicleMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public VehicleDto getVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forVehicle(id));

        return vehicleMapper.toDto(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return vehicleMapper.toDtoList(vehicles);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getAvailableVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
        return vehicleMapper.toDtoList(vehicles);
    }

    @Transactional
    public VehicleDto updateVehicle(Long id, VehicleDto vehicleDto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forVehicle(id));

        vehicle.setLicensePlate(vehicleDto.getLicensePlate());
        vehicle.setCapacityKg(vehicleDto.getCapacityKg());

        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("Updated vehicle ID {}", id);

        return vehicleMapper.toDto(updated);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw ResourceNotFoundException.forVehicle(id);
        }

        vehicleRepository.deleteById(id);
        log.info("Deleted vehicle ID {}", id);
    }
}
