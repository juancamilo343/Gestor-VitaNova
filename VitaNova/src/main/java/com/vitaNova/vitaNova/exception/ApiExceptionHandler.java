package com.vitaNova.vitaNova.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones de los controladores REST a respuestas JSON con el
 * codigo HTTP correspondiente, dejando siempre rastro en el log.
 */
@RestControllerAdvice(annotations = RestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({RecursoNoEncontradoException.class, EmptyResultDataAccessException.class})
    public ResponseEntity<ApiError> noEncontrado(Exception ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violacion de integridad de datos en {}", request.getRequestURI(), ex);
        return respuesta(HttpStatus.CONFLICT,
                "La operacion viola una restriccion de la base de datos", request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> accesoDatos(DataAccessException ex, HttpServletRequest request) {
        log.error("Error de acceso a datos en {}", request.getRequestURI(), ex);
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible acceder a la base de datos", request);
    }

    @ExceptionHandler(AlmacenamientoArchivoException.class)
    public ResponseEntity<ApiError> almacenamiento(AlmacenamientoArchivoException ex, HttpServletRequest request) {
        log.error("Error al almacenar archivos en {}", request.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ApiError> respuesta(HttpStatus status, String mensaje, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), mensaje, request.getRequestURI()));
    }
}
