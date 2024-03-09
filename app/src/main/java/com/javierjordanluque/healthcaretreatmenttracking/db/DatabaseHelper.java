package com.javierjordanluque.healthcaretreatmenttracking.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.javierjordanluque.healthcaretreatmenttracking.R;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBInitializingException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.ExceptionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "healthcare_treatment_tracking.db";
    private static final int DATABASE_VERSION = 1;
    private final String DATABASE_SCRIPT = "healthcare_treatment_tracking.sql";
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            executeSQLScript(db, DATABASE_SCRIPT);
        } catch (DBInitializingException exception) {
            ExceptionManager.advertiseUI(context, context.getString(R.string.error_initializing_app));
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Method to upgrade database
    }

    private void executeSQLScript(SQLiteDatabase db, String scriptFile) throws DBInitializingException {
        try {
            InputStream inputStream = context.getAssets().open(scriptFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder statement = new StringBuilder();
            String line;
            int lineNumber = 0;

            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;
                if (!line.trim().isEmpty()) {
                    statement.append(line);
                    if (line.endsWith(";")) {
                        try {
                            db.execSQL(statement.toString());
                        } catch (SQLException exception) {
                            throw new DBInitializingException("Failed to execute SQL statement on line (" + lineNumber + ") :" + line + " from SQLite database script " +
                                    "(" + DATABASE_SCRIPT + ")", exception);
                        }
                        statement.setLength(0);
                    }
                }
            }
            bufferedReader.close();
        } catch (IOException exception) {
            throw new DBInitializingException("Failed to manage SQLite database script (" + DATABASE_SCRIPT + ") inside assets folder", exception);
        }
    }
}
