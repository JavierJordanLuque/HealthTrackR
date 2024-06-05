package com.javierjordanluque.healthtrackr.util.exceptions;

public class DBDeleteException extends Exception {
    /** @noinspection FieldCanBeLocal*/
    private final String TAG = "DELETE";

    public DBDeleteException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause);
    }
}
