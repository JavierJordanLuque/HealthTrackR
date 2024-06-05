package com.javierjordanluque.healthtrackr.util.exceptions;

public class DeserializationException extends Exception {
    /** @noinspection FieldCanBeLocal*/
    private final String TAG = "DESERIALIZATION";

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause);
    }
}
