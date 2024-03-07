package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.User;
import com.javierjordanluque.healthcaretreatmenttracking.models.UserCredentials;
import com.javierjordanluque.healthcaretreatmenttracking.models.enumerations.BloodType;
import com.javierjordanluque.healthcaretreatmenttracking.models.enumerations.Gender;
import com.javierjordanluque.healthcaretreatmenttracking.util.SerializationUtils;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.CipherData;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.HashData;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SecurityService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;

public class UserRepository extends BaseRepository<User> {
    private static final String TABLE_NAME = "USER";
    private final String ID = "id";
    private final String EMAIL = "email";
    private final String EMAIL_IV = "email_iv";
    private final String EMAIL_HASH = "email_hash";
    private final String PASSWORD = "password";
    private final String PASSWORD_SALT = "password_salt";
    private final String FULL_NAME = "full_name";
    private final String FULL_NAME_IV = "full_name_iv";
    private final String BIRTH_DATE = "birth_date";
    private final String GENDER = "gender";
    private final String BLOOD_TYPE = "blood_type";
    private final String BLOOD_TYPE_IV = "blood_type_iv";

    public UserRepository(Context context) {
        super(TABLE_NAME, context);
    }

    @Override
    protected ContentValues getContentValues(User user) {
        ContentValues contentValues = new ContentValues();

        CipherData cipherData;
        if (user.getEmail() != null) {
            try {
                cipherData = SecurityService.encrypt(SerializationUtils.serialize(user.getEmail()));
                contentValues.put(EMAIL, cipherData.getEncryptedData());
                contentValues.put(EMAIL_IV, cipherData.getInitializationVector());
                contentValues.put(EMAIL_HASH, SecurityService.hash(SerializationUtils.serialize(user.getEmail())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (user.getFullName() != null) {
            try {
                cipherData = SecurityService.encrypt(SerializationUtils.serialize(user.getFullName()));
                contentValues.put(FULL_NAME, cipherData.getEncryptedData());
                contentValues.put(FULL_NAME_IV, cipherData.getInitializationVector());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (user.getBirthDate() != null)
            contentValues.put(BIRTH_DATE, user.getBirthDate().atStartOfDay(ZoneOffset.UTC).toInstant().getEpochSecond());
        if (user.getGender() != null)
            contentValues.put(GENDER, user.getGender().name());
        if (user.getBloodType() != null) {
            try {
                cipherData = SecurityService.encrypt(SerializationUtils.serialize(user.getBloodType()));
                contentValues.put(BLOOD_TYPE, cipherData.getEncryptedData());
                contentValues.put(BLOOD_TYPE_IV, cipherData.getInitializationVector());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected User cursorToItem(Cursor cursor) {
        CipherData cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(EMAIL)), cursor.getBlob(cursor.getColumnIndex(EMAIL_IV)));
        String email = null;
        try {
            email = (String) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(FULL_NAME)), cursor.getBlob(cursor.getColumnIndex(FULL_NAME_IV)));
        String fullName = null;
        try {
            fullName = (String) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        User user = new User(email, fullName);
        user.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        if (!cursor.isNull(cursor.getColumnIndex(BIRTH_DATE)))
            user.setBirthDate(Instant.ofEpochSecond(cursor.getLong(cursor.getColumnIndex(BIRTH_DATE))).atZone(ZoneOffset.UTC).toLocalDate());

        String gender = cursor.getString(cursor.getColumnIndex(GENDER));
        if (gender != null)
            user.setGender(Gender.valueOf(gender));

        byte[] bloodTypeBytes = cursor.getBlob(cursor.getColumnIndex(BLOOD_TYPE));
        byte[] bloodTypeIV = cursor.getBlob(cursor.getColumnIndex(BLOOD_TYPE_IV));
        if (bloodTypeBytes != null && bloodTypeIV != null) {
            cipherData = new CipherData(bloodTypeBytes, bloodTypeIV);
            try {
                BloodType bloodType = (BloodType) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), BloodType.class);
                user.setBloodType(bloodType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return user;
    }

    @SuppressLint({"Range", "Recycle"})
    public UserCredentials findUserCredentials(String email) {
        SQLiteDatabase db = open();

        try {
            byte[] emailHash = SecurityService.hash(SerializationUtils.serialize(email));

            String selection = EMAIL_HASH + "=?";
            String[] selectionArgs = {new String(emailHash, StandardCharsets.UTF_8)};
            Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                byte[] passwordBytes = cursor.getBlob(cursor.getColumnIndex(PASSWORD));
                byte[] salt =  cursor.getBlob(cursor.getColumnIndex(PASSWORD_SALT));
                if (passwordBytes != null && salt != null) {
                    HashData hashData = new HashData(passwordBytes, salt);
                    return new UserCredentials(cursor.getLong(cursor.getColumnIndex(ID)), hashData);
                }
            }

            close(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateUserCredentials(UserCredentials userCredentials) {
        SQLiteDatabase db = open();

        ContentValues values = new ContentValues();
        values.put(PASSWORD, userCredentials.getHashData().getHashedData());
        values.put(PASSWORD_SALT, userCredentials.getHashData().getSalt());

        long id = userCredentials.getUserId();
        String selection = "id=?";
        String[] selectionArgs = {String.valueOf(id)};
        db.update(TABLE_NAME, values, selection, selectionArgs);

        close(db);
    }
}
