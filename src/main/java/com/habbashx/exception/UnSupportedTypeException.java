package com.habbashx.exception;

import java.io.Serial;

/**
 * Represents an exception that is thrown when an unsupported data type is encountered.
 *
 * This exception is a subclass of {@code RuntimeException}, indicating that it is
 * unchecked and does not need to be declared in the {@code throws} clause of a method.
 * It is typically used in scenarios where a data type is not supported for processing,
 * and an appropriate error needs to be communicated.
 *
 * The exception message should provide details about the unsupported data type that
 * triggered the exception.
 */
public class UnSupportedTypeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5821265119369261068L;

    public UnSupportedTypeException(String message) {
        super(message);
    }
}
