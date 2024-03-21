package com.javierjordanluque.healthtrackr.util.exceptions;

public class NotificationException extends Exception {
    private final String TAG = "NOTIFICATION";

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause.getClass().getSimpleName());
    }
}