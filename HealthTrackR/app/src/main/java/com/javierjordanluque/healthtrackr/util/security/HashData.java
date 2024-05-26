package com.javierjordanluque.healthtrackr.util.security;

public class HashData {
    private final byte[] hashedData;
    private final byte[] salt;

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
