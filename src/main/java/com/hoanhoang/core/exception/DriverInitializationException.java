package com.hoanhoang.core.exception;

/**
 * Thrown when WebDriver initialization fails. Wraps underlying exceptions with descriptive error
 * messages.
 */
public class DriverInitializationException extends RuntimeException {

    /**
     * Creates a new driver initialization exception with message.
     *
     * @param message error message describing what went wrong
     */
    public DriverInitializationException(String message) {
        super(message);
    }

    /**
     * Creates a new driver initialization exception with message and cause.
     *
     * @param message error message describing what went wrong
     * @param cause the underlying exception that caused this exception
     */
    public DriverInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
