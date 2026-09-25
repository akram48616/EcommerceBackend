package org.example.ecommercebackend.Exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, Integer available) {
        super("Insufficient stock for '" + productName + "'. Available: " + available);
    }
    public InsufficientStockException(String message){
        super(message);
    }
}