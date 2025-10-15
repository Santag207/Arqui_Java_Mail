package com.envio_correo.email.exceptions;

public class OTNServiceException extends Exception {
    public OTNServiceException(String message) {
        super(message);
    }

    public OTNServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}