package com.javierjordanluque.healthtrackr.util.security;

public class CipherData {
    private byte[] encryptedData;
    private byte[] initializationVector;

    public CipherData(byte[] encryptedData, byte[] initializationVector) {
        this.encryptedData = encryptedData;
        this.initializationVector = initializationVector;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public byte[] getInitializationVector() {
        return initializationVector;
    }
}
