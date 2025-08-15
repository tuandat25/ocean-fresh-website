package com.tuandat.oceanfresh_backend.exceptions;

public class SocialLoginException extends RuntimeException {
    public SocialLoginException(String message) {
        super(message);
    }
    
    public SocialLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
