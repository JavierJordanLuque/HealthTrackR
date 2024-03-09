package com.javierjordanluque.healthcaretreatmenttracking.util.exceptions;

public class AlarmException extends Exception {
    private final String TAG = "ALARM";

    public AlarmException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause.getClass().getSimpleName());
    }
}
