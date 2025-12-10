package com.logistics.logiroute.mapper;

import com.logistics.logiroute.domain.entity.DeliveryRoute;
import com.logistics.logiroute.dto.DeliveryRouteDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PackageMapper.class})
public interface DeliveryRouteMapper {

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleLicensePlate", source = "vehicle.licensePlate")
    @Mapping(target = "totalWeight", expression = "java(deliveryRoute.getTotalWeight())")
    DeliveryRouteDto toDto(DeliveryRoute deliveryRoute);

    @Mapping(target = "vehicle", ignore = true)
    DeliveryRoute toEntity(DeliveryRouteDto deliveryRouteDto);

    List<DeliveryRouteDto> toDtoList(List<DeliveryRoute> deliveryRoutes);
}
