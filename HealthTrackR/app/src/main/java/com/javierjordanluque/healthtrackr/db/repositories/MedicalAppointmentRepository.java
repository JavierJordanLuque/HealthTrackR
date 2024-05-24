package com.javierjordanluque.healthtrackr.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.javierjordanluque.healthtrackr.db.BaseRepository;
import com.javierjordanluque.healthtrackr.models.Location;
import com.javierjordanluque.healthtrackr.models.MedicalAppointment;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.util.security.SerializationUtils;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DecryptionException;
import com.javierjordanluque.healthtrackr.util.exceptions.DeserializationException;
import com.javierjordanluque.healthtrackr.util.exceptions.EncryptionException;
import com.javierjordanluque.healthtrackr.util.exceptions.SerializationException;
import com.javierjordanluque.healthtrackr.util.security.CipherData;
import com.javierjordanluque.healthtrackr.util.security.SecurityService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MedicalAppointmentRepository extends BaseRepository<MedicalAppointment> {
    private static final String TABLE_NAME = "MEDICAL_APPOINTMENT";
    private final String TREATMENT_ID = "treatment_id";
    private final String SUBJECT = "subject";
    private final String DATE_TIME = "date_time";
    private final String DATE_TIME_IV = "date_time_iv";
    private final String PLACE = "place";
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

        if (appointment.getDateTime() != null) {
            CipherData cipherData = SecurityService.encrypt(SerializationUtils.serialize(appointment.getDateTime().toEpochSecond()));
            contentValues.put(DATE_TIME, cipherData.getEncryptedData());
            contentValues.put(DATE_TIME_IV, cipherData.getInitializationVector());
        }

        if (appointment.getSubject() != null) {
            if (appointment.getSubject().isEmpty()) {
                contentValues.putNull(SUBJECT);
            } else {
                contentValues.put(SUBJECT, appointment.getSubject());
            }
        }

        if (appointment.getLocation() != null) {
            if (appointment.getLocation().getPlace() == null || appointment.getLocation().getPlace().isEmpty()) {
                contentValues.putNull(PLACE);

                if (appointment.getLocation().getLatitude() == Double.MIN_VALUE && appointment.getLocation().getLongitude() == Double.MIN_VALUE) {
                    contentValues.putNull(LATITUDE);
                    contentValues.putNull(LONGITUDE);
                } else {
                    contentValues.put(LATITUDE, appointment.getLocation().getLatitude());
                    contentValues.put(LONGITUDE, appointment.getLocation().getLongitude());
                }
            } else {
                contentValues.put(PLACE, appointment.getLocation().getPlace());

                contentValues.putNull(LATITUDE);
                contentValues.putNull(LONGITUDE);
            }
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
                ZoneId.systemDefault());

        Location location = null;
        if (cursor.getString(cursor.getColumnIndex(PLACE)) != null) {
            location = new Location(cursor.getString(cursor.getColumnIndex(PLACE)), null, null);
        } else if (!cursor.isNull(cursor.getColumnIndex(LATITUDE)) && !cursor.isNull(cursor.getColumnIndex(LONGITUDE))) {
            location = new Location(null, cursor.getDouble(cursor.getColumnIndex(LATITUDE)), cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));
        }

        MedicalAppointment appointment = new MedicalAppointment(null, treatment, date, cursor.getString(cursor.getColumnIndex(SUBJECT)), location);
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
