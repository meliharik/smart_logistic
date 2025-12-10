package com.logistics.logiroute.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException forVehicle(Long vehicleId) {
        return new ResourceNotFoundException(
                String.format("Vehicle with ID %d not found", vehicleId)
        );
    }

    public static ResourceNotFoundException forPackage(Long packageId) {
        return new ResourceNotFoundException(
                String.format("Package with ID %d not found", packageId)
        );
    }

    public static ResourceNotFoundException forDeliveryRoute(Long routeId) {
        return new ResourceNotFoundException(
                String.format("Delivery route with ID %d not found", routeId)
        );
    }
}
