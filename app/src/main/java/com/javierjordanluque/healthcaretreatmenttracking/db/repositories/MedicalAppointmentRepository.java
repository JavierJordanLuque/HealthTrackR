package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.Location;
import com.javierjordanluque.healthcaretreatmenttracking.models.MedicalAppointment;
import com.javierjordanluque.healthcaretreatmenttracking.models.Treatment;
import com.javierjordanluque.healthcaretreatmenttracking.util.SerializationUtils;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBFindException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBInsertException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DecryptionException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DeserializationException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.EncryptionException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.SerializationException;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.CipherData;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SecurityService;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class MedicalAppointmentRepository extends BaseRepository<MedicalAppointment> {
    private static final String TABLE_NAME = "MEDICAL_APPOINTMENT";
    private final String TREATMENT_ID = "treatment_id";
    private final String PURPOSE = "purpose";
    private final String DATE_TIME = "date_time";
    private final String DATE_TIME_IV = "date_time_iv";
    private final String LATITUDE = "latitude";
    private final String LONGITUDE = "longitude";
    private final Context context;

    public MedicalAppointmentRepository(Context context) {
        super(TABLE_NAME, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(MedicalAppointment appointment) throws SerializationException, EncryptionException {
        ContentValues contentValues = new ContentValues();

        if (appointment.getTreatment() != null)
            contentValues.put(TREATMENT_ID, appointment.getTreatment().getId());
        if (appointment.getPurpose() != null)
            contentValues.put(PURPOSE, appointment.getPurpose());
        if (appointment.getDateTime() != null) {
            CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(appointment.getDateTime().toEpochSecond()));
            contentValues.put(DATE_TIME, cipherData.getEncryptedData());
            contentValues.put(DATE_TIME_IV, cipherData.getInitializationVector());
        }
        if (appointment.getLocation() != null) {
            contentValues.put(LATITUDE, appointment.getLocation().getLatitude());
            contentValues.put(LONGITUDE, appointment.getLocation().getLongitude());
        }

        return contentValues;
    }

    @Override
    @SuppressLint("Range")
    protected MedicalAppointment cursorToItem(Cursor cursor) throws DBFindException, DecryptionException, DeserializationException, DBInsertException, DBDeleteException {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        Treatment treatment = treatmentRepository.findById(cursor.getLong(cursor.getColumnIndex(TREATMENT_ID)));

        CipherData cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(DATE_TIME)), cursor.getBlob(cursor.getColumnIndex(DATE_TIME_IV)));
        ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochSecond((Long) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), Long.class)),
                TimeZone.getDefault().toZoneId());

        Location location = null;
        if (!cursor.isNull(cursor.getColumnIndex(LATITUDE)) && !cursor.isNull(cursor.getColumnIndex(LONGITUDE)))
            location = new Location(cursor.getDouble(cursor.getColumnIndex(LATITUDE)), cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));

        MedicalAppointment appointment = new MedicalAppointment(null, treatment, cursor.getString(cursor.getColumnIndex(PURPOSE)), date, location);
        appointment.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return appointment;
    }

    public List<MedicalAppointment> findTreatmentAppointments(long treatmentId) throws DBFindException {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<MedicalAppointment> appointments = new ArrayList<>();

        try {
            db = open();
            String selection = TREATMENT_ID + "=?";
            String[] selectionArgs = {String.valueOf(treatmentId)};
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext())
                    appointments.add(cursorToItem(cursor));
            }
        } catch (SQLiteException | DBFindException | DecryptionException |
                 DeserializationException | DBInsertException | DBDeleteException exception) {
            throw new DBFindException("Failed to findTreatmentAppointments from treatment with id (" + treatmentId + ")", exception);
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return appointments;
    }
}
