package com.habbashx.exception;

import java.io.Serial;

/**
 * This class represents an exception that is thrown when an empty list is encountered
 * during processing. It is typically used to signal an error when a list that is expected
 * to contain elements is found to be empty.
 *
 * This exception is a subclass of {@code RuntimeException}, making it unchecked,
 * meaning it does not need to be explicitly declared in method signatures or caught by the caller.
 */
public class EmptyListException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = -8412912728110373216L;

    public EmptyListException(String message) {
        super(message);
    }

}
