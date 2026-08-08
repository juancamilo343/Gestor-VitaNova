package com.vitaNova.vitaNova.exception;

/**
 * Se lanza cuando un recurso solicitado por id no existe en la base de datos.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " con id " + id + " no existe");
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
