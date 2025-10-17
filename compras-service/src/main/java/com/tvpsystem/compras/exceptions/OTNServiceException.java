package com.tvpsystem.compras.exceptions;

public class OTNServiceException extends RuntimeException {
    public OTNServiceException(String message) {
        super(message);
    }

    public OTNServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}