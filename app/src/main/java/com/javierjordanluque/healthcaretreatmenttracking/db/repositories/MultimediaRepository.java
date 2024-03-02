package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.Multimedia;
import com.javierjordanluque.healthcaretreatmenttracking.models.Step;
import com.javierjordanluque.healthcaretreatmenttracking.models.enumerations.MultimediaType;

import java.util.ArrayList;
import java.util.List;

public class MultimediaRepository extends BaseRepository<Multimedia> {
    private static final String TABLE_NAME = "MULTIMEDIA";
    private final String ID = "id";
    private final String STEP_ID = "step_id";
    private final String TYPE = "type";
    private final String PATH = "path";
    private Context context;

    public MultimediaRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Multimedia multimedia) {
        ContentValues contentValues = new ContentValues();

        if (multimedia.getStep() != null)
            contentValues.put(STEP_ID, multimedia.getStep().getId());
        if (multimedia.getType() != null)
            contentValues.put(TYPE, multimedia.getType().name());
        if (multimedia.getPath() != null)
            contentValues.put(PATH, multimedia.getPath());

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Multimedia cursorToItem(Cursor cursor) {
        StepRepository stepRepository = new StepRepository(context);
        Step step = stepRepository.findById(cursor.getLong(cursor.getColumnIndex(STEP_ID)));

        Multimedia multimedia = new Multimedia(null, step, MultimediaType.valueOf(cursor.getString(cursor.getColumnIndex(TYPE))), cursor.getString(cursor.getColumnIndex(PATH)));
        multimedia.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return multimedia;
    }

    public List<Multimedia> findStepMultimedias(long stepId) {
        List<Multimedia> multimedias = new ArrayList<>();
        SQLiteDatabase db = open();

        String selection = STEP_ID + "=?";
        String[] selectionArgs = {String.valueOf(stepId)};
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Multimedia multimedia = cursorToItem(cursor);
                multimedias.add(multimedia);
            }
            cursor.close();
        }

        close(db);
        return multimedias;
    }
}
