package com.payment_api_service.exception;

public class ResourceAlreadyExistsException extends RuntimeException{
    public ResourceAlreadyExistsException(){
        super();
    }
    public ResourceAlreadyExistsException(String message){
        super(message);
    }
    public ResourceAlreadyExistsException(String message, Throwable cause){
        super(message, cause);
    }
}
