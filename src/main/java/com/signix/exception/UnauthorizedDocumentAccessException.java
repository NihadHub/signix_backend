package com.signix.exception;

public class UnauthorizedDocumentAccessException extends RuntimeException {
    public UnauthorizedDocumentAccessException() {
        super("Vous n'êtes pas autorisé à accéder à ce document");
    }
}
