package com.javierjordanluque.healthcaretreatmenttracking.models;

import com.javierjordanluque.healthcaretreatmenttracking.util.security.HashData;

public class UserCredentials {
    private final long userId;
    private final HashData hashData;

    public UserCredentials(long userId, HashData hashData) {
        this.userId = userId;
        this.hashData = hashData;
    }

    public long getUserId() {
        return userId;
    }

    public HashData getHashData() {
        return hashData;
    }
}
