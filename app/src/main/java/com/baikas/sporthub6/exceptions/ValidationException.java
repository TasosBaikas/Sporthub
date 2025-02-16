package com.baikas.sporthub6.exceptions;

public class ValidationException extends RuntimeException{

    private String internalMessage = "";

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, String internalMessage) {
        super(message);

        this.internalMessage = internalMessage;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public String getInternalMessage() {
        return internalMessage;
    }
}
