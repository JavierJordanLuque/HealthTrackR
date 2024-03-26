package com.javierjordanluque.healthtrackr.util.exceptions;

public class DBInitializingException extends Exception {
    private final String TAG = "DB INIT";

    public DBInitializingException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause);
    }
}
