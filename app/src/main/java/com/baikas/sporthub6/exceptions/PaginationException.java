package com.baikas.sporthub6.exceptions;

public class PaginationException extends RuntimeException{
    public PaginationException() {
        super();
    }

    public PaginationException(String message) {
        super(message);
    }

}
