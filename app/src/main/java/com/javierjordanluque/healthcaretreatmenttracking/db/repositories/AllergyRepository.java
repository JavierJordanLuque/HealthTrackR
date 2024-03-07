package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.Allergy;
import com.javierjordanluque.healthcaretreatmenttracking.models.User;
import com.javierjordanluque.healthcaretreatmenttracking.util.SerializationUtils;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.CipherData;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SecurityService;

import java.util.ArrayList;
import java.util.List;

public class AllergyRepository extends BaseRepository<Allergy> {
    private static final String TABLE_NAME = "ALLERGY";
    private final String USER_ID = "user_id";
    private final String NAME = "name";
    private final String NAME_IV = "name_iv";
    private Context context;

    public AllergyRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Allergy allergy) {
        ContentValues contentValues = new ContentValues();

        if (allergy.getUser() != null)
            contentValues.put(USER_ID, allergy.getUser().getId());
        if (allergy.getName() != null) {
            try {
                CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(allergy.getName()));
                contentValues.put(NAME, cipherData.getEncryptedData());
                contentValues.put(NAME_IV, cipherData.getInitializationVector());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Allergy cursorToItem(Cursor cursor) {
        UserRepository userRepository = new UserRepository(context);
        User user = userRepository.findById(cursor.getLong(cursor.getColumnIndex(USER_ID)));

        CipherData cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(NAME)), cursor.getBlob(cursor.getColumnIndex(NAME_IV)));
        String name = null;
        try {
            name = (String) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Allergy allergy = new Allergy(user, name);
        allergy.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return allergy;
    }

    public List<Allergy> findUserAllergies(long userId) {
        List<Allergy> allergies = new ArrayList<>();
        SQLiteDatabase db = open();
        Cursor cursor = null;

        try {
            String selection = USER_ID + "=?";
            String[] selectionArgs = {String.valueOf(userId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Allergy allergy = cursorToItem(cursor);
                    allergies.add(allergy);
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return allergies;
    }
}
