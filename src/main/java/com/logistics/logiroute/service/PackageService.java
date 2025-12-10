package com.logistics.logiroute.service;

import com.logistics.logiroute.domain.entity.Package;
import com.logistics.logiroute.domain.enums.PackageStatus;
import com.logistics.logiroute.dto.PackageDto;
import com.logistics.logiroute.exception.ResourceNotFoundException;
import com.logistics.logiroute.mapper.PackageMapper;
import com.logistics.logiroute.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {

    private final PackageRepository packageRepository;
    private final PackageMapper packageMapper;

    @Transactional
    public PackageDto createPackage(PackageDto packageDto) {
        Package pkg = packageMapper.toEntity(packageDto);
        pkg.setStatus(PackageStatus.CREATED);

        Package saved = packageRepository.save(pkg);
        log.info("Created package ID {}", saved.getId());

        return packageMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public PackageDto getPackage(Long id) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forPackage(id));

        return packageMapper.toDto(pkg);
    }

    @Transactional(readOnly = true)
    public List<PackageDto> getAllPackages() {
        List<Package> packages = packageRepository.findAll();
        return packageMapper.toDtoList(packages);
    }

    @Transactional(readOnly = true)
    public List<PackageDto> getUnassignedPackages() {
        List<Package> packages = packageRepository.findByDeliveryRouteIsNull();
        return packageMapper.toDtoList(packages);
    }

    @Transactional(readOnly = true)
    public List<PackageDto> getPackagesByStatus(PackageStatus status) {
        List<Package> packages = packageRepository.findByStatus(status);
        return packageMapper.toDtoList(packages);
    }

    @Transactional
    public PackageDto updatePackage(Long id, PackageDto packageDto) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forPackage(id));

        pkg.setDeliveryAddress(packageDto.getDeliveryAddress());
        pkg.setWeightKg(packageDto.getWeightKg());
        pkg.setDeliveryDeadline(packageDto.getDeliveryDeadline());

        Package updated = packageRepository.save(pkg);
        log.info("Updated package ID {}", id);

        return packageMapper.toDto(updated);
    }

    @Transactional
    public void deletePackage(Long id) {
        if (!packageRepository.existsById(id)) {
            throw ResourceNotFoundException.forPackage(id);
        }

        packageRepository.deleteById(id);
        log.info("Deleted package ID {}", id);
    }
}
