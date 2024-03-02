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

import java.time.LocalDate;

public class UserRepository extends BaseRepository<User> {
    private static final String TABLE_NAME = "USER";
    private final String ID = "id";
    private final String EMAIL = "email";
    private final String PASSWORD = "password";
    private final String SALT = "salt";
    private final String FULL_NAME = "full_name";
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

        if (user.getEmail() != null)
            contentValues.put(EMAIL, user.getEmail());
        if (user.getFullName() != null)
            contentValues.put(FULL_NAME, user.getFullName());
        if (user.getBirthDate() != null)
            contentValues.put(BIRTH_DATE, user.getBirthDate().toString());
        if (user.getGender() != null)
            contentValues.put(GENDER, user.getGender().name());
        if (user.getBloodType() != null) {
            try {
                CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(user.getBloodType()));
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
        User user = new User(cursor.getString(cursor.getColumnIndex(EMAIL)), cursor.getString(cursor.getColumnIndex(FULL_NAME)));
        user.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        String birth_date = cursor.getString(cursor.getColumnIndex(BIRTH_DATE));
        if (birth_date != null)
            user.setBirthDate(LocalDate.parse(birth_date));

        String gender = cursor.getString(cursor.getColumnIndex(GENDER));
        if (gender != null)
            user.setGender(Gender.valueOf(gender));

        byte[] blood_type = cursor.getBlob(cursor.getColumnIndex(BLOOD_TYPE));
        byte[] blood_type_iv = cursor.getBlob(cursor.getColumnIndex(BLOOD_TYPE_IV));
        if (blood_type != null && blood_type_iv != null) {
            CipherData cipherData = new CipherData(blood_type, blood_type_iv);
            try {
                BloodType bloodType = (BloodType) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), BloodType.class);
                user.setBloodType(bloodType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return user;
    }

    @SuppressLint("Range")
    public UserCredentials findUserCredentials(String userEmail) {
        SQLiteDatabase db = open();

        String selection = EMAIL + "=?";
        String[] selectionArgs = {userEmail};
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            byte[] password = cursor.getBlob(cursor.getColumnIndex(PASSWORD));
            byte[] salt =  cursor.getBlob(cursor.getColumnIndex(SALT));
            if (password != null && salt != null) {
                HashData hashData = new HashData(password, salt);
                return new UserCredentials(cursor.getLong(cursor.getColumnIndex(ID)), hashData);
            }
        }

        close(db);
        return null;
    }

    public void updateUserCredentials(UserCredentials userCredentials) {
        SQLiteDatabase db = open();

        ContentValues values = new ContentValues();
        values.put(PASSWORD, userCredentials.getHashData().getHashedPassword());
        values.put(SALT, userCredentials.getHashData().getSalt());

        long id = userCredentials.getUserId();
        String selection = "id=?";
        String[] selectionArgs = {String.valueOf(id)};
        db.update(TABLE_NAME, values, selection, selectionArgs);

        close(db);
    }
}
