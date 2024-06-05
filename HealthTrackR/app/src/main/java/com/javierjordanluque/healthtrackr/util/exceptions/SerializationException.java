package com.javierjordanluque.healthtrackr.util.exceptions;

public class SerializationException extends Exception {
    /** @noinspection FieldCanBeLocal*/
    private final String TAG = "SERIALIZATION";

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause);
    }
}
