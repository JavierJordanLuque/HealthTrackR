package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.Symptom;
import com.javierjordanluque.healthcaretreatmenttracking.models.Treatment;
import com.javierjordanluque.healthcaretreatmenttracking.util.SerializationUtils;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBFindException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBInsertException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DecryptionException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DeserializationException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.EncryptionException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.SerializationException;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.CipherData;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SecurityService;

import java.util.ArrayList;
import java.util.List;

public class SymptomRepository extends BaseRepository<Symptom> {
    private static final String TABLE_NAME = "SYMPTOM";
    private final String TREATMENT_ID = "treatment_id";
    private final String DESCRIPTION = "description";
    private final String DESCRIPTION_IV = "description_iv";
    private Context context;

    public SymptomRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Symptom symptom) throws SerializationException, EncryptionException {
        ContentValues contentValues = new ContentValues();

        if (symptom.getTreatment() != null)
            contentValues.put(TREATMENT_ID, symptom.getTreatment().getId());
        if (symptom.getDescription() != null) {
            CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(symptom.getDescription()));
            contentValues.put(DESCRIPTION, cipherData.getEncryptedData());
            contentValues.put(DESCRIPTION_IV, cipherData.getInitializationVector());
        }

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Symptom cursorToItem(Cursor cursor) throws DBFindException, DecryptionException, DeserializationException, DBInsertException {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        Treatment treatment = treatmentRepository.findById(cursor.getLong(cursor.getColumnIndex(TREATMENT_ID)));

        CipherData cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(DESCRIPTION)), cursor.getBlob(cursor.getColumnIndex(DESCRIPTION_IV)));
        String description = (String) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), String.class);

        Symptom symptom = new Symptom(null, treatment, description);
        symptom.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return symptom;
    }

    public List<Symptom> findTreatmentSymptoms(long treatmentId) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Symptom> symptoms = new ArrayList<>();

        try {
            db = open();
            String selection = TREATMENT_ID + "=?";
            String[] selectionArgs = {String.valueOf(treatmentId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext())
                    symptoms.add(cursorToItem(cursor));
            }
        } catch (SQLiteException | DBFindException | DecryptionException |
                 DeserializationException | DBInsertException exception) {
            throw new DBFindException("Failed to findTreatmentSymptoms from treatment with id (" + treatmentId + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return symptoms;
    }
}
