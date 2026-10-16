package com.example.hensley_weighttracker.service;

public class ServiceResult<T> {
    private final boolean success;
    private final T value;
    private final String message;

    private ServiceResult(boolean success, T value, String message) {
        this.success = success;
        this.value = value;
        this.message = message;
    }

    public static <T> ServiceResult<T> success(T value, String message) {
        return new ServiceResult<>(true, value, message);
    }

    public static <T> ServiceResult<T> failure(String message) {
        return new ServiceResult<>(false, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
