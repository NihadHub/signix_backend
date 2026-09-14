package com.signix.exception;

public class AlreadySignedException extends RuntimeException {
    public AlreadySignedException() {
        super("Ce document a déjà été signé");
    }
}