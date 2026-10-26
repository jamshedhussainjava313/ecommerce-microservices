package com.ecommerce.order_service.client;

public class ProductNotFoundException extends RuntimeException{

    public ProductNotFoundException(Long productId){
        super("Product not found with id: " + productId);
    }
}
