package com.baikas.sporthub6.exceptions;

public class NoInternetConnectionException extends RuntimeException{

    public NoInternetConnectionException() {
        super();
    }
    public NoInternetConnectionException(String message) {
        super(message);
    }

    public NoInternetConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoInternetConnectionException(Throwable cause) {
        super(cause);
    }
}
