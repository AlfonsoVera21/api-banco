package com.api.apibanco.infrastructure.response;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        LocalDateTime timestamp,
        int status,
        String message,
        T data
) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(LocalDateTime.now(), 200, message, data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(LocalDateTime.now(), 201, message, data);
    }
}
