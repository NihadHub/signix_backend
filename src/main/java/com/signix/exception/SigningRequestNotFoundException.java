package com.signix.exception;

public class SigningRequestNotFoundException extends RuntimeException {
    public SigningRequestNotFoundException(String token) {
        super("Demande de signature introuvable pour ce lien");
    }
}