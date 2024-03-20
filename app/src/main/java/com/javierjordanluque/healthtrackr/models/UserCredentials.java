package com.javierjordanluque.healthtrackr.models;

import com.javierjordanluque.healthtrackr.util.exceptions.HashException;
import com.javierjordanluque.healthtrackr.util.exceptions.SerializationException;
import com.javierjordanluque.healthtrackr.util.security.HashData;
import com.javierjordanluque.healthtrackr.util.security.SecurityService;
import com.javierjordanluque.healthtrackr.util.security.SerializationUtils;

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
