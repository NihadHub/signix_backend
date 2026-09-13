package com.signix.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(Long id) {
        super("Document introuvable avec l'id: " + id);
    }
}