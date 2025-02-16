package com.baikas.sporthub6.exceptions;

public class MaxSizeReachedException extends RuntimeException{
    public MaxSizeReachedException() {
    }

    public MaxSizeReachedException(String message) {
        super(message);
    }
}
