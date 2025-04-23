package com.tuandat.oceanfresh_backend.exceptions;
public class PermissionDenyException extends Exception{
    public PermissionDenyException(String message) {
        super(message);
    }
}