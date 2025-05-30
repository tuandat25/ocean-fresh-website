package com.tuandat.oceanfresh_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Insufficient stock for %s with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
