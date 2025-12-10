package com.logistics.logiroute.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Vehicle is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @OneToMany(mappedBy = "deliveryRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Package> packages = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime completedAt;

    public void addPackage(Package pkg) {
        packages.add(pkg);
        pkg.setDeliveryRoute(this);
    }

    public void removePackage(Package pkg) {
        packages.remove(pkg);
        pkg.setDeliveryRoute(null);
    }

    public double getTotalWeight() {
        return packages.stream()
                .mapToDouble(Package::getWeightKg)
                .sum();
    }
}
