package com.baikas.sporthub6.models.result;

public abstract class Result<T> {

    private Result() {
    }

    public static final class Success<T> extends Result<T> {
        private final T item;

        public Success(T item) {
            this.item = item;
        }

        public T getItem() {
            return item;
        }
    }

    public static final class Failure<T> extends Result<T> {
        private final Throwable error;

        public Failure(Throwable error) {
            this.error = error;
        }

        public Throwable getThrowable() {
            return error;
        }
    }
}
