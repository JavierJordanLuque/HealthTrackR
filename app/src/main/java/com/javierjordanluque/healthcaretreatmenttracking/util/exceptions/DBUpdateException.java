package com.javierjordanluque.healthcaretreatmenttracking.util.exceptions;

public class DBUpdateException extends Exception {
    private final String TAG = "UPDATE";

    public DBUpdateException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause.getClass().getSimpleName());
    }
}
