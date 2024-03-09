package com.javierjordanluque.healthcaretreatmenttracking.util.exceptions;

public class DBInsertException extends Exception {
    private final String TAG = "INSERT";

    public DBInsertException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause.getClass().getSimpleName());
    }
}
