package com.ecommerce.order_service.client;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(Long userId){
        super("User not found with id: "+userId);
    }
}
