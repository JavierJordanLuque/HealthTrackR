package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.MedicalAppointment;
import com.javierjordanluque.healthcaretreatmenttracking.models.Medicine;
import com.javierjordanluque.healthcaretreatmenttracking.models.Notification;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBFindException;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository extends BaseRepository<Notification> {
    private static final String TABLE_NAME = "NOTIFICATION";
    private final String TREATMENT_ID = "treatment_id";
    private final String MEDICINE_ID = "medicine_id";
    private final String MEDICAL_APPOINTMENT_ID = "medical_appointment_id";
    private final String TIMESTAMP = "timestamp";
    private Context context;

    public NotificationRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Notification notification) {
        ContentValues contentValues = new ContentValues();

        if (notification.getMedicine() != null) {
            contentValues.put(TREATMENT_ID, notification.getMedicine().getTreatment().getId());
            contentValues.put(MEDICINE_ID, notification.getMedicine().getId());
        }
        if (notification.getAppointment() != null)
            contentValues.put(MEDICAL_APPOINTMENT_ID, notification.getAppointment().getId());

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected Notification cursorToItem(Cursor cursor) throws DBFindException {
        long timestamp = cursor.getLong(cursor.getColumnIndex(TIMESTAMP));

        Notification notification = null;
        if (!cursor.isNull(cursor.getColumnIndex(TREATMENT_ID)) && !cursor.isNull(cursor.getColumnIndex(MEDICINE_ID))) {
            MedicineRepository medicineRepository = new MedicineRepository(context);
            Medicine medicine = medicineRepository.findById(cursor.getLong(cursor.getColumnIndex(TREATMENT_ID)), cursor.getLong(cursor.getColumnIndex(MEDICINE_ID)));
            notification = new Notification(medicine, timestamp);
        } else if (!cursor.isNull(cursor.getColumnIndex(MEDICAL_APPOINTMENT_ID))) {
            MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
            MedicalAppointment appointment = medicalAppointmentRepository.findById(cursor.getLong(cursor.getColumnIndex(MEDICAL_APPOINTMENT_ID)));
            notification = new Notification(appointment, timestamp);
        }

        if (notification != null)
            notification.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return notification;
    }

    public List<Notification> findMedicineNotifications(long treatmentId, long medicineId) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Notification> notifications = new ArrayList<>();

        try {
            db = open();
            String selection = TREATMENT_ID + "=? and " + MEDICINE_ID + "=?";
            String[] selectionArgs = {String.valueOf(treatmentId), String.valueOf(medicineId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext())
                    notifications.add(cursorToItem(cursor));
            }
        } catch (SQLiteException | DBFindException exception) {
            throw new DBFindException("Failed to findMedicineNotifications from medicine with treatmentId (" + treatmentId + ") and medicineId (" + medicineId + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return notifications;
    }

    @SuppressLint("Range")
    public Notification findAppointmentNotification(long appointmentId) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Notification notification = null;

        try {
            db = open();
            String selection = MEDICAL_APPOINTMENT_ID + "=?";
            String[] selectionArgs = {String.valueOf(appointmentId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst())
                notification = cursorToItem(cursor);
        } catch (SQLiteException | DBFindException exception) {
            throw new DBFindException("Failed to findAppointmentNotification from appointment with id (" + appointmentId + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return notification;
    }
}
