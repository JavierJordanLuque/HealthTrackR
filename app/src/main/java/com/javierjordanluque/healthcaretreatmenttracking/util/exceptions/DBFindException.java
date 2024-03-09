package com.javierjordanluque.healthcaretreatmenttracking.util.exceptions;

public class DBFindException extends Exception {
    private final String TAG = "FIND";

    public DBFindException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause.getClass().getSimpleName());
    }
}
