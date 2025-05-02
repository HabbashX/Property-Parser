package com.habbashx.exception;

import java.io.Serial;

public class UnSupportedTypeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5821265119369261068L;

    public UnSupportedTypeException(String message) {
        super(message);
    }
}
