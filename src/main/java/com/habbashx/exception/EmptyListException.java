package com.habbashx.exception;

import java.io.Serial;

public class EmptyListException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = -8412912728110373216L;

    public EmptyListException(String message) {
        super(message);
    }

}
