package com.habbashx.exception;

import java.io.Serial;

/**
 * This class represents an exception that is thrown when an empty item is encountered
 * during processing. It typically indicates a failure caused by invalid or unexpected
 * input, specifically in cases where an empty value is not allowed.
 *
 * This exception is a subclass of {@code RuntimeException}, so it is unchecked and
 * does not require mandatory handling by the caller.
 */
public class EmptyItemException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8731720696893165065L;

    public EmptyItemException(String message) {
        super(message);
    }
}
