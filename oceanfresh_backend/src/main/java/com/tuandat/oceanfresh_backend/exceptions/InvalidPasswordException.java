package com.tuandat.oceanfresh_backend.exceptions;

public class InvalidPasswordException extends Exception{
    public InvalidPasswordException(String message) {
        super(message);
    }
}