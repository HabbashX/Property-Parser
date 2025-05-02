package com.habbashx.exception;

import java.io.Serial;

public class EmptyItemException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8731720696893165065L;

    public EmptyItemException(String message) {
        super(message);
    }
}
