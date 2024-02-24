package com.javierjordanluque.healthcaretreatmenttracking.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.javierjordanluque.healthcaretreatmenttracking.models.Identifiable;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRepository<T extends Identifiable> {
    private final Context context;
    private final DatabaseHelper databaseHelper;

    public BaseRepository(Context context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
    }

    protected SQLiteDatabase open() throws SQLException {
        return databaseHelper.getWritableDatabase();
    }

    protected void close(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    protected abstract String getTableName();
    protected abstract ContentValues getContentValues(T item);
    protected abstract T cursorToItem(Cursor cursor);

    public long insert(T item) {
        SQLiteDatabase db = null;
        try {
            db = open();
            ContentValues values = getContentValues(item);
            return db.insert(getTableName(), null, values);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            close(db);
        }
    }

    public void update(T item) {
        SQLiteDatabase db = null;
        try {
            db = open();
            ContentValues values = getContentValues(item);
            long id = item.getId();
            String selection = "id=?";
            String[] selectionArgs = {String.valueOf(id)};
            db.update(getTableName(), values, selection, selectionArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(db);
        }
    }

    public void delete(T item) {
        SQLiteDatabase db = null;
        try {
            db = open();
            long id = item.getId();
            String selection = "id=?";
            String[] selectionArgs = {String.valueOf(id)};
            db.delete(getTableName(), selection, selectionArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(db);
        }
    }

    public T getById(long id) {
        SQLiteDatabase db = null;
        try {
            db = open();
            String selection = "id=?";
            String[] selectionArgs = {String.valueOf(id)};
            Cursor cursor = db.query(getTableName(), null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToItem(cursor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(db);
        }
        return null;
    }

    public List<T> getAll() {
        List<T> items = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            db = open();
            Cursor cursor = db.query(getTableName(), null, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    T item = cursorToItem(cursor);
                    items.add(item);
                }
                cursor.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(db);
        }
        return items;
    }
}