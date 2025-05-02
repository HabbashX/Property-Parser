package com.habbashx.exception;

import java.io.Serial;

public class InvalidEnumerationValueException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 777719660099257922L;

    public InvalidEnumerationValueException(String message) {
        super(message);
    }
}
