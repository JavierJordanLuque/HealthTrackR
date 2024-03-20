package com.javierjordanluque.healthtrackr.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthtrackr.db.BaseRepository;
import com.javierjordanluque.healthtrackr.models.Question;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

import java.util.ArrayList;
import java.util.List;

public class QuestionRepository extends BaseRepository<Question> {
    private static final String TABLE_NAME = "QUESTION";
    private final String TREATMENT_ID = "treatment_id";
    private final String DESCRIPTION = "description";
    private final Context context;

    public QuestionRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Question question) {
        ContentValues contentValues = new ContentValues();

        if (question.getTreatment() != null)
            contentValues.put(TREATMENT_ID, question.getTreatment().getId());
        if (question.getDescription() != null)
            contentValues.put(DESCRIPTION, question.getDescription());

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Question cursorToItem(Cursor cursor) throws DBFindException, DBInsertException {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        Treatment treatment = treatmentRepository.findById(cursor.getLong(cursor.getColumnIndex(TREATMENT_ID)));

        Question question = new Question(null, treatment, cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
        question.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return question;
    }

    public List<Question> findTreatmentQuestions(long treatmentId) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Question> questions = new ArrayList<>();

        try {
            db = open();
            String selection = TREATMENT_ID + "=?";
            String[] selectionArgs = {String.valueOf(treatmentId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext())
                    questions.add(cursorToItem(cursor));
            }
        } catch (SQLiteException | DBFindException | DBInsertException exception) {
            throw new DBFindException("Failed to findTreatmentQuestions from treatment with id (" + treatmentId + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return questions;
    }
}
