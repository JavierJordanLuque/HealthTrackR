package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.Location;
import com.javierjordanluque.healthcaretreatmenttracking.models.MedicalAppointment;
import com.javierjordanluque.healthcaretreatmenttracking.models.Treatment;
import com.javierjordanluque.healthcaretreatmenttracking.util.SerializationUtils;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.CipherData;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SecurityService;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class MedicalAppointmentRepository extends BaseRepository<MedicalAppointment> {
    private static final String TABLE_NAME = "MEDICAL_APPOINTMENT";
    private final String ID = "id";
    private final String TREATMENT_ID = "treatment_id";
    private final String PURPOSE = "purpose";
    private final String DATE_TIME = "date_time";
    private final String DATE_TIME_IV = "date_time_iv";
    private final String LATITUDE = "latitude";
    private final String LONGITUDE = "longitude";
    private Context context;

    public MedicalAppointmentRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(MedicalAppointment appointment) {
        ContentValues contentValues = new ContentValues();

        if (appointment.getTreatment() != null)
            contentValues.put(TREATMENT_ID, appointment.getTreatment().getId());
        if (appointment.getPurpose() != null) {
            contentValues.put(PURPOSE, appointment.getPurpose());
        }
        if (appointment.getDateTime() != null) {
            try {
                CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(appointment.getDateTime().toEpochSecond()));
                contentValues.put(DATE_TIME, cipherData.getEncryptedData());
                contentValues.put(DATE_TIME_IV, cipherData.getInitializationVector());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (appointment.getLocation() != null) {
            contentValues.put(LATITUDE, appointment.getLocation().getLatitude());
            contentValues.put(LONGITUDE, appointment.getLocation().getLongitude());
        }

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected MedicalAppointment cursorToItem(Cursor cursor) {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        Treatment treatment = treatmentRepository.findById(cursor.getLong(cursor.getColumnIndex(TREATMENT_ID)));

        CipherData cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(DATE_TIME)), cursor.getBlob(cursor.getColumnIndex(DATE_TIME_IV)));
        ZonedDateTime dateTime = null;
        try {
            dateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond((Long) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), Long.class)), TimeZone.getDefault().toZoneId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Location location = null;
        if (!cursor.isNull(cursor.getColumnIndex(LATITUDE)) && !cursor.isNull(cursor.getColumnIndex(LONGITUDE)))
            location = new Location(cursor.getDouble(cursor.getColumnIndex(LATITUDE)), cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));

        MedicalAppointment appointment = new MedicalAppointment(null, treatment, cursor.getString(cursor.getColumnIndex(PURPOSE)), dateTime, location);
        appointment.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return appointment;
    }

    public List<MedicalAppointment> findTreatmentAppointments(long treatmentId) {
        List<MedicalAppointment> appointments = new ArrayList<>();
        SQLiteDatabase db = open();

        String selection = TREATMENT_ID + "=?";
        String[] selectionArgs = {String.valueOf(treatmentId)};
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                MedicalAppointment appointment = cursorToItem(cursor);
                appointments.add(appointment);
            }
            cursor.close();
        }

        close(db);
        return appointments;
    }
}
