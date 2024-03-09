package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.Step;
import com.javierjordanluque.healthcaretreatmenttracking.models.Treatment;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBFindException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBInsertException;

import java.util.ArrayList;
import java.util.List;

public class StepRepository extends BaseRepository<Step> {
    private static final String TABLE_NAME = "STEP";
    private final String TREATMENT_ID = "treatment_id";
    private final String TITLE = "title";
    private final String DESCRIPTION = "description";
    private final String NUM_ORDER = "num_order";
    private Context context;

    public StepRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Step step) {
        ContentValues contentValues = new ContentValues();

        if (step.getTreatment() != null)
            contentValues.put(TREATMENT_ID, step.getTreatment().getId());
        if (step.getTitle() != null)
            contentValues.put(TITLE, step.getTitle());
        if (step.getDescription() != null)
            contentValues.put(DESCRIPTION, step.getDescription());
        if (step.getNumOrder() != null)
            contentValues.put(NUM_ORDER, step.getNumOrder());

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Step cursorToItem(Cursor cursor) throws DBFindException, DBInsertException {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        Treatment treatment = treatmentRepository.findById(cursor.getLong(cursor.getColumnIndex(TREATMENT_ID)));

        Step step = new Step(null, treatment, cursor.getString(cursor.getColumnIndex(TITLE)), cursor.getString(cursor.getColumnIndex(DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(NUM_ORDER)));
        step.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return step;
    }

    public List<Step> findTreatmentSteps(long treatmentId) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Step> steps = new ArrayList<>();

        try {
            db = open();
            String selection = TREATMENT_ID + "=?";
            String[] selectionArgs = {String.valueOf(treatmentId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext())
                    steps.add(cursorToItem(cursor));
            }
        } catch (SQLiteException | DBFindException | DBInsertException exception) {
            throw new DBFindException("Failed to findTreatmentSteps from treatment with id (" + treatmentId + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return steps;
    }
}
