package com.javierjordanluque.healthtrackr.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthtrackr.models.Identifiable;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.DecryptionException;
import com.javierjordanluque.healthtrackr.util.exceptions.DeserializationException;
import com.javierjordanluque.healthtrackr.util.exceptions.EncryptionException;
import com.javierjordanluque.healthtrackr.util.exceptions.HashException;
import com.javierjordanluque.healthtrackr.util.exceptions.SerializationException;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRepository<T extends Identifiable> {
    private final String TABLE_NAME;
    protected final String ID = "id";
    private final DatabaseHelper databaseHelper;

    public BaseRepository(String tableName, Context context) {
        TABLE_NAME = tableName;
        databaseHelper = new DatabaseHelper(context);
    }

    protected SQLiteDatabase open() throws SQLiteException {
        return databaseHelper.getWritableDatabase();
    }

    protected void close(SQLiteDatabase db) {
        if (db != null && db.isOpen())
            db.close();
    }

    protected abstract ContentValues getContentValues(T item) throws SerializationException, EncryptionException, HashException;
    protected abstract T cursorToItem(Cursor cursor) throws DBFindException, DecryptionException, DeserializationException, DBInsertException, DBDeleteException;

    public long insert(T item) throws DBInsertException {
        SQLiteDatabase db = null;
        long insertedId;

        try {
            db = open();
            ContentValues values = getContentValues(item);

            insertedId = db.insert(TABLE_NAME, null, values);
        } catch (SQLiteException | SerializationException | EncryptionException | HashException exception) {
            throw new DBInsertException("Failed to insert item (" + item.getClass().getSimpleName() + ")", exception);
        } finally {
            close(db);
        }

        return insertedId;
    }

    public void update(T item) throws DBUpdateException {
        SQLiteDatabase db = null;
        long id = item.getId();

        try {
            db = open();
            ContentValues values = getContentValues(item);
            String selection = ID + "=?";
            String[] selectionArgs = {String.valueOf(id)};

            db.update(TABLE_NAME, values, selection, selectionArgs);
        } catch (SQLiteException | SerializationException | EncryptionException | HashException exception) {
            throw new DBUpdateException("Failed to update item (" + item.getClass().getSimpleName() + ") with id (" + id + ")", exception);
        } finally {
            close(db);
        }
    }

    public void delete(T item) throws DBDeleteException {
        SQLiteDatabase db = null;
        long id = item.getId();

        try {
            db = open();
            String selection = ID + "=?";
            String[] selectionArgs = {String.valueOf(id)};

            db.delete(TABLE_NAME, selection, selectionArgs);
        } catch (SQLiteException exception) {
            throw new DBDeleteException("Failed to delete item (" + item.getClass().getSimpleName() + ") with id (" + id + ")", exception);
        } finally {
            close(db);
        }
    }

    public T findById(long id) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        T item = null;

        try {
            db = open();
            String selection = ID + "=?";
            String[] selectionArgs = {String.valueOf(id)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst())
                item = cursorToItem(cursor);
        } catch (SQLiteException | DBFindException | DecryptionException |
                 DeserializationException | DBInsertException | DBDeleteException exception) {
            throw new DBFindException("Failed to findById item with id (" + id + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return item;
    }

    public List<T> findAll() throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<T> items = new ArrayList<>();

        try {
            db = open();
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    T item = cursorToItem(cursor);
                    items.add(item);
                }
            }
        } catch (SQLiteException | DBFindException | DecryptionException |
                 DeserializationException | DBInsertException | DBDeleteException exception) {
            throw new DBFindException("Failed to findAll items", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return items;
    }
}
