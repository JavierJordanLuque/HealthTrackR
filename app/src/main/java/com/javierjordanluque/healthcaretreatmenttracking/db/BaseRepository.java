package com.javierjordanluque.healthcaretreatmenttracking.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthcaretreatmenttracking.models.Identifiable;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRepository<T extends Identifiable> {
    private final String TABLE_NAME;
    private final DatabaseHelper databaseHelper;

    public BaseRepository(String tableName, Context context) {
        TABLE_NAME = tableName;
        databaseHelper = new DatabaseHelper(context);
    }

    protected SQLiteDatabase open() throws SQLiteException {
        return databaseHelper.getWritableDatabase();
    }

    protected void close(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    protected abstract ContentValues getContentValues(T item);
    protected abstract T cursorToItem(Cursor cursor);

    public long insert(T item) {
        SQLiteDatabase db = open();

        ContentValues values = getContentValues(item);
        long insertedId = db.insert(TABLE_NAME, null, values);

        close(db);
        return insertedId;
    }

    public void update(T item) {
        SQLiteDatabase db = open();

        ContentValues values = getContentValues(item);
        long id = item.getId();
        String selection = "id=?";
        String[] selectionArgs = {String.valueOf(id)};
        db.update(TABLE_NAME, values, selection, selectionArgs);

        close(db);
    }

    public void delete(T item) {
        SQLiteDatabase db = open();

        long id = item.getId();
        String selection = "id=?";
        String[] selectionArgs = {String.valueOf(id)};
        db.delete(TABLE_NAME, selection, selectionArgs);

        close(db);
    }

    public T findById(long id) {
        SQLiteDatabase db = open();

        String selection = "id=?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst())
            return cursorToItem(cursor);

        close(db);
        return null;
    }

    public List<T> findAll() {
        List<T> items = new ArrayList<>();
        SQLiteDatabase db = open();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                T item = cursorToItem(cursor);
                items.add(item);
            }
            cursor.close();
        }

        close(db);
        return items;
    }
}
