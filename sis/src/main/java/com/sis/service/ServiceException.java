package com.sis.service;

/**
 * Thrown by the service layer when a DAO operation fails.
 *
 * <p>The message is always safe to display directly in a UI error dialog —
 * it never leaks raw SQL state, vendor error codes, or internal stack details.
 *
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
