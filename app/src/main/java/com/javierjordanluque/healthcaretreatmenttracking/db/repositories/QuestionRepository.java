package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.Question;
import com.javierjordanluque.healthcaretreatmenttracking.models.Treatment;

import java.util.ArrayList;
import java.util.List;

public class QuestionRepository extends BaseRepository<Question> {
    private static final String TABLE_NAME = "QUESTION";
    private final String TREATMENT_ID = "treatment_id";
    private final String DESCRIPTION = "description";
    private Context context;

    public QuestionRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Question question) {
        ContentValues contentValues = new ContentValues();

        if (question.getTreatment() != null)
            contentValues.put(TREATMENT_ID, question.getTreatment().getId());
        if (question.getDescription() != null) {
            contentValues.put(DESCRIPTION, question.getDescription());
        }

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Question cursorToItem(Cursor cursor) {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        Treatment treatment = treatmentRepository.findById(cursor.getLong(cursor.getColumnIndex(TREATMENT_ID)));

        Question question = new Question(null, treatment, cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
        question.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return question;
    }

    public List<Question> findTreatmentQuestions(long treatmentId) {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = open();
        Cursor cursor = null;

        try {
            String selection = TREATMENT_ID + "=?";
            String[] selectionArgs = {String.valueOf(treatmentId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Question question = cursorToItem(cursor);
                    questions.add(question);
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return questions;
    }
}
