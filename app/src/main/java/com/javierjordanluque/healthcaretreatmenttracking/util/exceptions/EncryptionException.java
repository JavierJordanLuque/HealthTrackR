package com.javierjordanluque.healthcaretreatmenttracking.util.exceptions;

public class EncryptionException extends Exception {
    private final String TAG = "ENCRYPTION";

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause.getClass().getSimpleName());
    }
}
