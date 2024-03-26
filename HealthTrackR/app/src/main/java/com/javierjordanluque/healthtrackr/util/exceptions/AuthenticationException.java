package com.javierjordanluque.healthtrackr.util.exceptions;

public class AuthenticationException extends Exception {
    private final String TAG = "AUTHENTICATION";

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause);
    }
}
