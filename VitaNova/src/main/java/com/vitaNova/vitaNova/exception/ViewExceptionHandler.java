package com.vitaNova.vitaNova.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Muestra una pagina de error legible (y registra la causa) cuando una pantalla
 * Thymeleaf falla, en lugar de dejar que el usuario vea la pagina blanca de error.
 */
@ControllerAdvice(annotations = Controller.class)
@Order(Ordered.LOWEST_PRECEDENCE)
public class ViewExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ViewExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ModelAndView noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());
        return paginaError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(AlmacenamientoArchivoException.class)
    public ModelAndView almacenamiento(AlmacenamientoArchivoException ex, HttpServletRequest request) {
        log.error("Error al almacenar archivos en {}", request.getRequestURI(), ex);
        return paginaError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ModelAndView accesoDatos(DataAccessException ex, HttpServletRequest request) {
        log.error("Error de acceso a datos en {}", request.getRequestURI(), ex);
        return paginaError(HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible acceder a la base de datos", request);
    }

    private ModelAndView paginaError(HttpStatus status, String mensaje, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error", status);
        mav.addObject("status", status.value());
        mav.addObject("error", status.getReasonPhrase());
        mav.addObject("message", mensaje);
        mav.addObject("path", request.getRequestURI());
        return mav;
    }
}
