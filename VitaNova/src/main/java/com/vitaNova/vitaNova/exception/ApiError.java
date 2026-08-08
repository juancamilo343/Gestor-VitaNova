package com.vitaNova.vitaNova.exception;

import java.time.OffsetDateTime;

/**
 * Cuerpo de respuesta uniforme para los errores de la API REST.
 */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path);
    }
}
