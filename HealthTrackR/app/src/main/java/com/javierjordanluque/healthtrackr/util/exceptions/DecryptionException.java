package com.javierjordanluque.healthtrackr.util.exceptions;

public class DecryptionException extends Exception {
    private final String TAG = "DECRYPTION";

    public DecryptionException(String message, Throwable cause) {
        super(message, cause);
        ExceptionManager.log(ExceptionManager.ERROR, TAG, getClass().getSimpleName(), message, cause);
    }
}
