package com.javierjordanluque.healthcaretreatmenttracking.util.exceptions;

public class DeserializationException extends Exception {
    private final String TAG = "DESERIALIZATION";

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause.getClass().getSimpleName());
    }
}
