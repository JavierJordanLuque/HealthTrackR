package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.Step;
import com.javierjordanluque.healthcaretreatmenttracking.models.Treatment;

import java.util.ArrayList;
import java.util.List;

public class StepRepository extends BaseRepository<Step> {
    private static final String TABLE_NAME = "STEP";
    private final String ID = "id";
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
    protected Step cursorToItem(Cursor cursor) {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        Treatment treatment = treatmentRepository.findById(cursor.getLong(cursor.getColumnIndex(TREATMENT_ID)));

        Step step = new Step(null, treatment, cursor.getString(cursor.getColumnIndex(TITLE)), cursor.getString(cursor.getColumnIndex(DESCRIPTION)), cursor.getInt(cursor.getColumnIndex(NUM_ORDER)));
        step.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return step;
    }

    public List<Step> findTreatmentSteps(long treatmentId) {
        List<Step> steps = new ArrayList<>();
        SQLiteDatabase db = open();

        String selection = TREATMENT_ID + "=?";
        String[] selectionArgs = {String.valueOf(treatmentId)};
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Step step = cursorToItem(cursor);
                steps.add(step);
            }
            cursor.close();
        }

        close(db);
        return steps;
    }
}
