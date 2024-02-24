package com.javierjordanluque.healthcaretreatmenttracking.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        } catch (IOException e) {
            e.printStackTrace();
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

    private void executeSQLScript(SQLiteDatabase db, String scriptFile) throws IOException {
        InputStream inputStream = context.getAssets().open(scriptFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder statement = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                statement.append(line);
                if (line.endsWith(";")) {
                    try {
                        db.execSQL(statement.toString());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    statement.setLength(0);
                }
            }
        }
        bufferedReader.close();
    }
}