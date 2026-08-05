package com.classapp.controller;

/** Thrown by the Application layer when a use case can't proceed because of bad input or a broken business rule. */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
