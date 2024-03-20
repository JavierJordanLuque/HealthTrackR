package com.javierjordanluque.healthtrackr.util.exceptions;

public class SecretKeyException extends Exception {
    private final String TAG = "KEY";

    public SecretKeyException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause.getClass().getSimpleName());
    }
}
