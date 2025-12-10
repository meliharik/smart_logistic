package com.logistics.logiroute.exception;

import com.logistics.logiroute.domain.enums.PackageStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidStatusTransitionException forPackage(Long packageId, PackageStatus currentStatus, PackageStatus targetStatus) {
        return new InvalidStatusTransitionException(
                String.format("Package ID %d cannot transition from %s to %s. Invalid state transition.",
                        packageId, currentStatus, targetStatus)
        );
    }
}
