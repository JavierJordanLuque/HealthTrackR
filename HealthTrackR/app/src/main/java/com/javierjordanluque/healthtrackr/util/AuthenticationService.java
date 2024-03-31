package com.javierjordanluque.healthtrackr.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.db.repositories.UserRepository;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.UserCredentials;
import com.javierjordanluque.healthtrackr.util.exceptions.AuthenticationException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.HashException;
import com.javierjordanluque.healthtrackr.util.exceptions.SerializationException;
import com.javierjordanluque.healthtrackr.util.security.SecurityService;
import com.javierjordanluque.healthtrackr.util.security.SerializationUtils;

public class AuthenticationService {
    private static final String PREFS_NAME = "HealthTrackR";
    private static final String PREFS_EMAIL = "email";
    private static final String PREFS_PASSWORD = "password";

    public static User register(Context context, String email, String password, String firstName, String lastName) throws AuthenticationException {
        User user;

        try {
            UserRepository userRepository = new UserRepository(context);
            UserCredentials userCredentials = userRepository.findUserCredentials(email);

            if (userCredentials != null) {
                throw new AuthenticationException(context.getString(R.string.error_existing_email), null);
            } else if (!SecurityService.meetsEmailRequirements(email)) {
                throw new AuthenticationException(context.getString(R.string.error_invalid_email_requirements), null);
            } else if (!SecurityService.meetsPasswordRequirements(password)) {
                throw new AuthenticationException(context.getString(R.string.authentication_password_requirements), null);
            } else {
                user = new User(email, firstName, lastName);
                user.setId(userRepository.insert(user));
                userRepository.updateUserCredentials(new UserCredentials(user.getId(), SecurityService.hashWithSalt(SerializationUtils.serialize(password))));
            }
        } catch (DBFindException | DBInsertException | SerializationException | HashException | DBUpdateException exception) {
            throw new AuthenticationException("Failed to register with the following credentials: Email (" + email + "), Password (" + password + ")", exception);
        }

        return user;
    }

    public static User login(Context context, String email, String password) throws AuthenticationException {
        User user;

        try {
            UserRepository userRepository = new UserRepository(context);
            UserCredentials userCredentials = userRepository.findUserCredentials(email);

            if (userCredentials == null) {
                throw new AuthenticationException(context.getString(R.string.error_incorrect_email), null);
            } else if (!userCredentials.equalsPassword(password)) {
                throw new AuthenticationException(context.getString(R.string.error_incorrect_password), null);
            } else {
                user = userRepository.findById(userCredentials.getUserId());
            }
        } catch (DBFindException | SerializationException | HashException exception) {
            throw new AuthenticationException("Failed to login with the following credentials: Email (" + email + "), Password (" + password + ")", exception);
        }

        return user;
    }

    public static void logout(User user) {
        if (user != null) {
            user.setId(-1);
            user.setEmail(null);
            user.setFirstName(null);
            user.setLastName(null);
            user.setBirthDate(null);
            user.setGender(null);
            user.setBloodType(null);
            user.setAllergies(null);
            user.setConditions(null);
            user.setTreatments(null);
        }
    }

    public static void saveCredentials(Context context, String email, String password) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFS_EMAIL, email);
        editor.putString(PREFS_PASSWORD, password);
        editor.apply();
    }

    public static void clearCredentials(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREFS_EMAIL);
        editor.remove(PREFS_PASSWORD);
        editor.apply();
    }

    public static Object[] getCredentials(Context context) {
        Object[] credentials = null;

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(AuthenticationService.PREFS_EMAIL, null);
        String password = sharedPreferences.getString(AuthenticationService.PREFS_PASSWORD, null);

        if (email != null && password != null)
            credentials = new Object[]{email, password};

        return credentials;
    }
}
