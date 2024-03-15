package com.javierjordanluque.healthcaretreatmenttracking.models;

import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.HashException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.SerializationException;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.HashData;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SecurityService;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SerializationUtils;

public class UserCredentials {
    private final long userId;
    private final HashData hashData;

    public UserCredentials(long userId, HashData hashData) {
        this.userId = userId;
        this.hashData = hashData;
    }

    public boolean equalsPassword(String password) throws SerializationException, HashException {
        return SecurityService.equalsHashAndData(hashData.getHashedData(), hashData.getSalt(), SerializationUtils.serialize(password));
    }

    public long getUserId() {
        return userId;
    }

    public HashData getHashData() {
        return hashData;
    }
}
