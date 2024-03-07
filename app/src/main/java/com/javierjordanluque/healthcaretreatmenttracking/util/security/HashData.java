package com.javierjordanluque.healthcaretreatmenttracking.util.security;

public class HashData {
    private byte[] hashedData;
    private byte[] salt;

    public HashData(byte[] hashedData, byte[] salt) {
        this.hashedData = hashedData;
        this.salt = salt;
    }

    public byte[] getHashedData() {
        return hashedData;
    }

    public byte[] getSalt() {
        return salt;
    }
}
