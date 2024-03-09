package com.javierjordanluque.healthcaretreatmenttracking.util.exceptions;

public class DBDeleteException extends Exception {
    private final String TAG = "DELETE";

    public DBDeleteException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause.getClass().getSimpleName());
    }
}
