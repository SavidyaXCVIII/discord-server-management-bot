package com.panikmode.discord_bot.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Object errors,
        Instant timestamp
) {
    // Constructor override to always set the timestamp automatically
    public ApiResponse {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    // --- Factory Methods for clean instantiation ---

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful");
    }

    public static <T> ApiResponse<T> error(String message, Object errors) {
        return new ApiResponse<>(false, message, null, errors, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }
}
