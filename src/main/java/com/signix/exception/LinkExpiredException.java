package com.signix.exception;

public class LinkExpiredException extends RuntimeException {
    public LinkExpiredException() {
        super("Ce lien de signature a expiré");
    }
}