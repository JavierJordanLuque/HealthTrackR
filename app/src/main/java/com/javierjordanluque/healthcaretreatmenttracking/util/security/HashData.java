package com.javierjordanluque.healthcaretreatmenttracking.util.security;

public class HashData {
    private byte[] hashedPassword;
    private byte[] salt;

    public HashData(byte[] hashedPassword, byte[] salt) {
        this.hashedPassword = hashedPassword;
        this.salt = salt;
    }

    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    public byte[] getSalt() {
        return salt;
    }
}
