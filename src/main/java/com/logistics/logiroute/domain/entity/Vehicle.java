package com.logistics.logiroute.domain.entity;

import com.logistics.logiroute.domain.enums.VehicleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "License plate is required")
    @Column(nullable = false, unique = true, length = 20)
    private String licensePlate;

    @NotNull(message = "Capacity is required")
    @PositiveOrZero(message = "Capacity must be positive or zero")
    @Column(nullable = false)
    private Double capacityKg;

    @NotNull(message = "Current load is required")
    @PositiveOrZero(message = "Current load must be positive or zero")
    @Column(nullable = false)
    @Builder.Default
    private Double currentLoadKg = 0.0;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryRoute> deliveryRoutes = new ArrayList<>();

    public double getRemainingCapacityKg() {
        return capacityKg - currentLoadKg;
    }

    public boolean canLoad(double weightKg) {
        return getRemainingCapacityKg() >= weightKg;
    }

    public void addLoad(double weightKg) {
        this.currentLoadKg += weightKg;
    }

    public void removeLoad(double weightKg) {
        this.currentLoadKg = Math.max(0, this.currentLoadKg - weightKg);
    }
}
