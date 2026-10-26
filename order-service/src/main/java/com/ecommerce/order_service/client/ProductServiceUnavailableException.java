package com.ecommerce.order_service.client;

public class ProductServiceUnavailableException extends RuntimeException{

    public ProductServiceUnavailableException(
            String message, Throwable cause) {
        super(message, cause);
    }
}
