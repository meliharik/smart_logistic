package com.logistics.logiroute.domain.entity;

import com.logistics.logiroute.domain.enums.PackageStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Delivery address is required")
    @Column(nullable = false, length = 500)
    private String deliveryAddress;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    @Column(nullable = false)
    private Double weightKg;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PackageStatus status = PackageStatus.CREATED;

    @NotNull(message = "Delivery deadline is required")
    @Column(nullable = false)
    private LocalDateTime deliveryDeadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_route_id")
    private DeliveryRoute deliveryRoute;

    public boolean canTransitionTo(PackageStatus newStatus) {
        if (this.status == newStatus) {
            return true;
        }

        return switch (this.status) {
            case CREATED -> newStatus == PackageStatus.LOADED;
            case LOADED -> newStatus == PackageStatus.DELIVERED;
            case DELIVERED -> false;
        };
    }
}
