package com.vitaNova.vitaNova.exception;

/**
 * Se lanza cuando falla la escritura de un archivo adjunto en disco.
 */
public class AlmacenamientoArchivoException extends RuntimeException {

    public AlmacenamientoArchivoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
