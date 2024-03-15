package com.javierjordanluque.healthcaretreatmenttracking.util.authentication;

import android.content.Context;

import com.javierjordanluque.healthcaretreatmenttracking.db.repositories.UserRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.User;
import com.javierjordanluque.healthcaretreatmenttracking.models.UserCredentials;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.AuthenticationException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBFindException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBInsertException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.HashException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.SerializationException;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SecurityService;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SerializationUtils;

public class AuthenticationService {
    public User register(Context context, String email, String password, String fullName) throws AuthenticationException {
        User user = null;

        try {
            UserRepository userRepository = new UserRepository(context);
            UserCredentials userCredentials = userRepository.findUserCredentials(email);

            if (userCredentials == null && SecurityService.meetsPasswordRequirements(password)) {
                user = new User(email, fullName);
                user.setId(userRepository.insert(user));
                userRepository.updateUserCredentials(new UserCredentials(user.getId(), SecurityService.hashWithSalt(SerializationUtils.serialize(password))));
            }
        } catch (DBFindException | DBInsertException | SerializationException | HashException | DBUpdateException exception) {
            throw new AuthenticationException("Failed to register with the following credentials: Email (" + email + "), Password (" + password + ")", exception);
        }

        return user;
    }

    public User login(Context context, String email, String password) throws AuthenticationException {
        User user = null;

        try {
            UserRepository userRepository = new UserRepository(context);
            UserCredentials userCredentials = userRepository.findUserCredentials(email);

            if (userCredentials != null && userCredentials.equalsPassword(password)) {
                user = userRepository.findById(userCredentials.getUserId());
            }
        } catch (DBFindException | SerializationException | HashException exception) {
            throw new AuthenticationException("Failed to login with the following credentials: Email (" + email + "), Password (" + password + ")", exception);
        }

        return user;
    }

    public void logout(User user) {
        if (user != null) {
            user.setId(-1);
            user.setEmail(null);
            user.setFullName(null);
            user.setBirthDate(null);
            user.setGender(null);
            user.setBloodType(null);
            user.setAllergies(null);
            user.setConditions(null);
            user.setTreatments(null);
        }
    }
}
