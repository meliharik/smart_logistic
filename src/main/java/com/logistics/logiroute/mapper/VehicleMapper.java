package com.logistics.logiroute.mapper;

import com.logistics.logiroute.domain.entity.Vehicle;
import com.logistics.logiroute.dto.VehicleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VehicleMapper {

    @Mapping(target = "remainingCapacityKg", expression = "java(vehicle.getRemainingCapacityKg())")
    VehicleDto toDto(Vehicle vehicle);

    @Mapping(target = "deliveryRoutes", ignore = true)
    Vehicle toEntity(VehicleDto vehicleDto);

    List<VehicleDto> toDtoList(List<Vehicle> vehicles);
}
