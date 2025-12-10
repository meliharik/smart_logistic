package com.logistics.logiroute.mapper;

import com.logistics.logiroute.domain.entity.Package;
import com.logistics.logiroute.dto.PackageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PackageMapper {

    @Mapping(target = "deliveryRouteId", source = "deliveryRoute.id")
    PackageDto toDto(Package pkg);

    @Mapping(target = "deliveryRoute", ignore = true)
    Package toEntity(PackageDto packageDto);

    List<PackageDto> toDtoList(List<Package> packages);
}
