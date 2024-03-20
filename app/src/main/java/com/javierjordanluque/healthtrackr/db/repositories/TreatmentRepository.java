package com.javierjordanluque.healthtrackr.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthtrackr.db.BaseRepository;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.util.security.SerializationUtils;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DecryptionException;
import com.javierjordanluque.healthtrackr.util.exceptions.DeserializationException;
import com.javierjordanluque.healthtrackr.util.exceptions.EncryptionException;
import com.javierjordanluque.healthtrackr.util.exceptions.SerializationException;
import com.javierjordanluque.healthtrackr.util.security.CipherData;
import com.javierjordanluque.healthtrackr.util.security.SecurityService;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class TreatmentRepository extends BaseRepository<Treatment> {
    private static final String TABLE_NAME = "TREATMENT";
    private final String USER_ID = "user_id";
    private final String TITLE = "title";
    private final String TITLE_IV = "title_iv";
    private final String START_DATE = "start_date";
    private final String START_DATE_IV = "start_date_iv";
    private final String END_DATE = "end_date";
    private final String END_DATE_IV = "end_date_iv";
    private final String DIAGNOSIS = "diagnosis";
    private final String DIAGNOSIS_IV = "diagnosis_iv";
    private final String CATEGORY = "category";
    private final String CATEGORY_IV = "category_iv";
    private final Context context;

    public TreatmentRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Treatment treatment) throws SerializationException, EncryptionException {
        ContentValues contentValues = new ContentValues();

        if (treatment.getUser() != null)
            contentValues.put(USER_ID, treatment.getUser().getId());
        if (treatment.getTitle() != null) {
            CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(treatment.getTitle()));
            contentValues.put(TITLE, cipherData.getEncryptedData());
            contentValues.put(TITLE_IV, cipherData.getInitializationVector());
        }
        if (treatment.getStartDate() != null) {
            CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(treatment.getStartDate().toEpochSecond()));
            contentValues.put(START_DATE, cipherData.getEncryptedData());
            contentValues.put(START_DATE_IV, cipherData.getInitializationVector());
        }
        if (treatment.getEndDate() != null) {
            CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(treatment.getEndDate().toEpochSecond()));
            contentValues.put(END_DATE, cipherData.getEncryptedData());
            contentValues.put(END_DATE_IV, cipherData.getInitializationVector());
        }
        if (treatment.getDiagnosis() != null) {
            CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(treatment.getDiagnosis()));
            contentValues.put(DIAGNOSIS, cipherData.getEncryptedData());
            contentValues.put(DIAGNOSIS_IV, cipherData.getInitializationVector());
        }
        if (treatment.getCategory() != null) {
            CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(treatment.getCategory()));
            contentValues.put(CATEGORY, cipherData.getEncryptedData());
            contentValues.put(CATEGORY_IV, cipherData.getInitializationVector());
        }

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Treatment cursorToItem(Cursor cursor) throws DBFindException, DecryptionException, DeserializationException, DBInsertException {
        UserRepository userRepository = new UserRepository(context);
        User user = userRepository.findById(cursor.getLong(cursor.getColumnIndex(USER_ID)));

        CipherData cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(TITLE)), cursor.getBlob(cursor.getColumnIndex(TITLE_IV)));
        String title = (String) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), String.class);

        cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(START_DATE)), cursor.getBlob(cursor.getColumnIndex(START_DATE_IV)));
        ZonedDateTime startDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond((Long) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), Long.class)),
                TimeZone.getDefault().toZoneId());

        byte[] endDateBytes = cursor.getBlob(cursor.getColumnIndex(END_DATE));
        byte[] endDateIV = cursor.getBlob(cursor.getColumnIndex(END_DATE_IV));
        ZonedDateTime endDate = null;
        if (endDateBytes != null && endDateIV != null) {
            cipherData = new CipherData(endDateBytes, endDateIV);
            endDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond((Long) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), Long.class)),
                    TimeZone.getDefault().toZoneId());
        }

        byte[] diagnosisBytes = cursor.getBlob(cursor.getColumnIndex(DIAGNOSIS));
        byte[] diagnosisIV = cursor.getBlob(cursor.getColumnIndex(DIAGNOSIS_IV));
        String diagnosis = null;
        if (diagnosisBytes != null && diagnosisIV != null) {
            cipherData = new CipherData(diagnosisBytes, diagnosisIV);
            diagnosis = (String) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), String.class);
        }

        byte[] categoryBytes = cursor.getBlob(cursor.getColumnIndex(CATEGORY));
        byte[] categoryIV = cursor.getBlob(cursor.getColumnIndex(CATEGORY_IV));
        TreatmentCategory category = null;
        if (categoryBytes != null && categoryIV != null) {
            cipherData = new CipherData(categoryBytes, categoryIV);
            category = (TreatmentCategory) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), TreatmentCategory.class);
        }

        Treatment treatment = new Treatment(null, user, title, startDate, endDate, diagnosis, category);
        treatment.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return treatment;
    }

    public List<Treatment> findUserTreatments(long userId) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Treatment> treatments = new ArrayList<>();

        try {
            db = open();
            String selection = USER_ID + "=?";
            String[] selectionArgs = {String.valueOf(userId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext())
                    treatments.add(cursorToItem(cursor));
            }
        } catch (SQLiteException | DBFindException | DecryptionException |
                 DeserializationException | DBInsertException exception) {
            throw new DBFindException("Failed to findUserTreatments from user with id (" + userId + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return treatments;
    }
}
