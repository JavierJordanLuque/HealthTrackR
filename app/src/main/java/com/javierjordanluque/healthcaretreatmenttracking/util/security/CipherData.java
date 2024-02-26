package com.javierjordanluque.healthcaretreatmenttracking.util.security;

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

    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }

    public byte[] getInitializationVector() {
        return initializationVector;
    }

    public void setInitializationVector(byte[] initializationVector) {
        this.initializationVector = initializationVector;
    }
}
