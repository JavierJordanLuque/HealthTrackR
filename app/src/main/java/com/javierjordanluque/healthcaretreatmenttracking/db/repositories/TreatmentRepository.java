package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.Treatment;
import com.javierjordanluque.healthcaretreatmenttracking.models.User;
import com.javierjordanluque.healthcaretreatmenttracking.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthcaretreatmenttracking.util.SerializationUtils;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.CipherData;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SecurityService;

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
    private Context context;

    public TreatmentRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Treatment treatment) {
        ContentValues contentValues = new ContentValues();

        if (treatment.getUser() != null)
            contentValues.put(USER_ID, treatment.getUser().getId());
        if (treatment.getTitle() != null) {
            try {
                CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(treatment.getTitle()));
                contentValues.put(TITLE, cipherData.getEncryptedData());
                contentValues.put(TITLE_IV, cipherData.getInitializationVector());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (treatment.getStartDate() != null) {
            try {
                CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(treatment.getStartDate().toEpochSecond()));
                contentValues.put(START_DATE, cipherData.getEncryptedData());
                contentValues.put(START_DATE_IV, cipherData.getInitializationVector());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (treatment.getEndDate() != null) {
            try {
                CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(treatment.getEndDate().toEpochSecond()));
                contentValues.put(END_DATE, cipherData.getEncryptedData());
                contentValues.put(END_DATE_IV, cipherData.getInitializationVector());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (treatment.getDiagnosis() != null) {
            try {
                CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(treatment.getDiagnosis()));
                contentValues.put(DIAGNOSIS, cipherData.getEncryptedData());
                contentValues.put(DIAGNOSIS_IV, cipherData.getInitializationVector());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (treatment.getCategory() != null) {
            try {
                CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(treatment.getCategory()));
                contentValues.put(CATEGORY, cipherData.getEncryptedData());
                contentValues.put(CATEGORY_IV, cipherData.getInitializationVector());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Treatment cursorToItem(Cursor cursor) {
        UserRepository userRepository = new UserRepository(context);
        User user = userRepository.findById(cursor.getLong(cursor.getColumnIndex(USER_ID)));

        CipherData cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(TITLE)), cursor.getBlob(cursor.getColumnIndex(TITLE_IV)));
        String title = null;
        try {
            title = (String) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(START_DATE)), cursor.getBlob(cursor.getColumnIndex(START_DATE_IV)));
        ZonedDateTime startDate = null;
        try {
            startDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond((Long) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), Long.class)), TimeZone.getDefault().toZoneId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] endDateBytes = cursor.getBlob(cursor.getColumnIndex(END_DATE));
        byte[] endDateIV = cursor.getBlob(cursor.getColumnIndex(END_DATE_IV));
        ZonedDateTime endDate = null;
        if (endDateBytes != null && endDateIV != null) {
            cipherData = new CipherData(endDateBytes, endDateIV);
            try {
                endDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond((Long) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), Long.class)), TimeZone.getDefault().toZoneId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        byte[] diagnosisBytes = cursor.getBlob(cursor.getColumnIndex(DIAGNOSIS));
        byte[] diagnosisIV = cursor.getBlob(cursor.getColumnIndex(DIAGNOSIS_IV));
        String diagnosis = null;
        if (diagnosisBytes != null && diagnosisIV != null) {
            cipherData = new CipherData(diagnosisBytes, diagnosisIV);
            try {
                diagnosis = (String) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), String.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        byte[] categoryBytes = cursor.getBlob(cursor.getColumnIndex(CATEGORY));
        byte[] categoryIV = cursor.getBlob(cursor.getColumnIndex(CATEGORY_IV));
        TreatmentCategory category = null;
        if (categoryBytes != null && categoryIV != null) {
            cipherData = new CipherData(categoryBytes, categoryIV);
            try {
                category = (TreatmentCategory) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), TreatmentCategory.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Treatment treatment = new Treatment(null, user, title, startDate, endDate, diagnosis, category);
        treatment.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return treatment;
    }

    public List<Treatment> findUserTreatments(long userId) {
        List<Treatment> treatments = new ArrayList<>();
        SQLiteDatabase db = open();

        try {
            String selection = USER_ID + "=?";
            String[] selectionArgs = {String.valueOf(userId)};
            Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Treatment treatment = cursorToItem(cursor);
                    treatments.add(treatment);
                }
                cursor.close();
            }
        } finally {
            close(db);
        }

        return treatments;
    }
}
