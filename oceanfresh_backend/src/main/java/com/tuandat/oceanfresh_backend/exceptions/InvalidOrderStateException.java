package com.tuandat.oceanfresh_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
    
    public InvalidOrderStateException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Invalid order state for %s with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
