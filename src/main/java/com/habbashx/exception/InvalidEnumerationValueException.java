package com.habbashx.exception;

import java.io.Serial;

/**
 * Represents an exception that is thrown when an invalid value is provided
 * for an enumeration type. This exception is typically used to signal that
 * a provided string value does not match any of the defined constants in a
 * specified enumeration.
 *
 * This exception is a subclass of {@code RuntimeException}, which means it
 * is unchecked and does not need to be declared in a method or constructor's
 * {@code throws} clause.
 *
 * The exception message should provide details about the invalid value and
 * the enumeration type for which the value was intended.
 */
public class InvalidEnumerationValueException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 777719660099257922L;

    public InvalidEnumerationValueException(String message) {
        super(message);
    }
}
