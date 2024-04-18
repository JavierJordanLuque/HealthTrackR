package com.javierjordanluque.healthtrackr.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthtrackr.db.BaseRepository;
import com.javierjordanluque.healthtrackr.models.Multimedia;
import com.javierjordanluque.healthtrackr.models.Guideline;
import com.javierjordanluque.healthtrackr.models.enumerations.MultimediaType;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

import java.util.ArrayList;
import java.util.List;

public class MultimediaRepository extends BaseRepository<Multimedia> {
    private static final String TABLE_NAME = "MULTIMEDIA";
    private final String GUIDELINE_ID = "guideline_id";
    private final String TYPE = "type";
    private final String PATH = "path";
    private final Context context;

    public MultimediaRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Multimedia multimedia) {
        ContentValues contentValues = new ContentValues();

        if (multimedia.getGuideline() != null)
            contentValues.put(GUIDELINE_ID, multimedia.getGuideline().getId());
        if (multimedia.getType() != null)
            contentValues.put(TYPE, multimedia.getType().name());
        if (multimedia.getPath() != null)
            contentValues.put(PATH, multimedia.getPath());

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Multimedia cursorToItem(Cursor cursor) throws DBFindException, DBInsertException {
        GuidelineRepository guidelineRepository = new GuidelineRepository(context);
        Guideline guideline = guidelineRepository.findById(cursor.getLong(cursor.getColumnIndex(GUIDELINE_ID)));

        Multimedia multimedia = new Multimedia(null, guideline, MultimediaType.valueOf(cursor.getString(cursor.getColumnIndex(TYPE))),
                cursor.getString(cursor.getColumnIndex(PATH)));
        multimedia.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return multimedia;
    }

    public List<Multimedia> findGuidelineMultimedias(long guidelineId) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Multimedia> multimedias = new ArrayList<>();

        try {
            db = open();
            String selection = GUIDELINE_ID + "=?";
            String[] selectionArgs = {String.valueOf(guidelineId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext())
                    multimedias.add(cursorToItem(cursor));
            }
        } catch (SQLiteException | DBFindException | DBInsertException exception) {
            throw new DBFindException("Failed to findGuidelineMultimedias from guideline with id (" + guidelineId + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return multimedias;
    }
}
