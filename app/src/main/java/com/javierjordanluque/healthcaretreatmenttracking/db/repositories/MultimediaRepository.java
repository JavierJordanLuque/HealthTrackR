package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.Multimedia;
import com.javierjordanluque.healthcaretreatmenttracking.models.Step;
import com.javierjordanluque.healthcaretreatmenttracking.models.enumerations.MultimediaType;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBFindException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBInsertException;

import java.util.ArrayList;
import java.util.List;

public class MultimediaRepository extends BaseRepository<Multimedia> {
    private static final String TABLE_NAME = "MULTIMEDIA";
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
    protected Multimedia cursorToItem(Cursor cursor) throws DBFindException, DBInsertException {
        StepRepository stepRepository = new StepRepository(context);
        Step step = stepRepository.findById(cursor.getLong(cursor.getColumnIndex(STEP_ID)));

        Multimedia multimedia = new Multimedia(null, step, MultimediaType.valueOf(cursor.getString(cursor.getColumnIndex(TYPE))), cursor.getString(cursor.getColumnIndex(PATH)));
        multimedia.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return multimedia;
    }

    public List<Multimedia> findStepMultimedias(long stepId) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Multimedia> multimedias = new ArrayList<>();

        try {
            db = open();
            String selection = STEP_ID + "=?";
            String[] selectionArgs = {String.valueOf(stepId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext())
                    multimedias.add(cursorToItem(cursor));
            }
        } catch (SQLiteException | DBFindException | DBInsertException exception) {
            throw new DBFindException("Failed to findStepMultimedias from step with id (" + stepId + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return multimedias;
    }
}
