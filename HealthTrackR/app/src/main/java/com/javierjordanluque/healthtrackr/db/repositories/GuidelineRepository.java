package com.javierjordanluque.healthtrackr.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthtrackr.db.BaseRepository;
import com.javierjordanluque.healthtrackr.models.Guideline;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

import java.util.ArrayList;
import java.util.List;

public class GuidelineRepository extends BaseRepository<Guideline> {
    private static final String TABLE_NAME = "GUIDELINE";
    private final String TREATMENT_ID = "treatment_id";
    private final String TITLE = "title";
    private final String DESCRIPTION = "description";
    private final String NUM_ORDER = "num_order";
    private final Context context;

    public GuidelineRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Guideline guideline) {
        ContentValues contentValues = new ContentValues();

        if (guideline.getTreatment() != null)
            contentValues.put(TREATMENT_ID, guideline.getTreatment().getId());

        if (guideline.getTitle() != null)
            contentValues.put(TITLE, guideline.getTitle());

        if (guideline.getDescription() != null) {
            if (guideline.getDescription().isEmpty()) {
                contentValues.putNull(DESCRIPTION);
            } else {
                contentValues.put(DESCRIPTION, guideline.getDescription());
            }
        }

        if (guideline.getNumOrder() != null)
            contentValues.put(NUM_ORDER, guideline.getNumOrder());

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Guideline cursorToItem(Cursor cursor) throws DBFindException, DBInsertException {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        Treatment treatment = treatmentRepository.findById(cursor.getLong(cursor.getColumnIndex(TREATMENT_ID)));

        Guideline guideline = new Guideline(null, treatment, cursor.getString(cursor.getColumnIndex(TITLE)), cursor.getString(cursor.getColumnIndex(DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(NUM_ORDER)));
        guideline.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return guideline;
    }

    public List<Guideline> findTreatmentGuidelines(long treatmentId) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Guideline> guidelines = new ArrayList<>();

        try {
            db = open();
            String selection = TREATMENT_ID + "=?";
            String[] selectionArgs = {String.valueOf(treatmentId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext())
                    guidelines.add(cursorToItem(cursor));
            }
        } catch (SQLiteException | DBFindException | DBInsertException exception) {
            throw new DBFindException("Failed to findTreatmentGuidelines from treatment with id (" + treatmentId + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return guidelines;
    }
}
