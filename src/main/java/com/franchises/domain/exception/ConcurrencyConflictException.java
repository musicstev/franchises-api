package com.franchises.domain.exception;

/**
 * Señala que el agregado fue modificado por otra operación entre su lectura y su
 * escritura. Es un concepto de dominio: protege los invariantes del agregado y no
 * depende de ninguna tecnología de persistencia concreta.
 */
public class ConcurrencyConflictException extends RuntimeException {

    public ConcurrencyConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
