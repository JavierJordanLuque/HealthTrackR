package com.javierjordanluque.healthtrackr.util.exceptions;

public class HashException extends Exception {
    /** @noinspection FieldCanBeLocal*/
    private final String TAG = "HASH";

    public HashException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause);
    }
}
