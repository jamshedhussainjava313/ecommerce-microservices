package com.ecommerce.order_service.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id){
        super("Order Not Found with id: "+id);
    }
}
