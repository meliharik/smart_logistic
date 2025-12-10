package com.logistics.logiroute.exception;

public class VehicleOverloadedException extends RuntimeException {

    public VehicleOverloadedException(String message) {
        super(message);
    }

    public VehicleOverloadedException(String message, Throwable cause) {
        super(message, cause);
    }

    public static VehicleOverloadedException forPackage(String licensePlate, double packageWeight, double remainingCapacity) {
        return new VehicleOverloadedException(
                String.format("Vehicle '%s' cannot load package of %.2f kg. Remaining capacity: %.2f kg",
                        licensePlate, packageWeight, remainingCapacity)
        );
    }
}
