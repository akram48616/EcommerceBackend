package org.example.ecommercebackend.Exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cannot checkout with an empty cart");
    }

    public EmptyCartException(String message) {
        super(message);
    }

    public EmptyCartException(String message, Throwable cause) {
        super(message, cause);
    }
}