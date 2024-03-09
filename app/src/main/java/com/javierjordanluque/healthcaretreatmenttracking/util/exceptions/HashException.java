package com.javierjordanluque.healthcaretreatmenttracking.util.exceptions;

public class HashException extends Exception {
    private final String TAG = "HASH";

    public HashException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause.getClass().getSimpleName());
    }
}
