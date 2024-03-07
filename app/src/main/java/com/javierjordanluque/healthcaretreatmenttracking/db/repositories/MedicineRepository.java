package com.javierjordanluque.healthcaretreatmenttracking.db.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.javierjordanluque.healthcaretreatmenttracking.db.BaseRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.BasicMedicine;
import com.javierjordanluque.healthcaretreatmenttracking.models.Medicine;
import com.javierjordanluque.healthcaretreatmenttracking.models.Treatment;
import com.javierjordanluque.healthcaretreatmenttracking.models.enumerations.AdministrationRoute;
import com.javierjordanluque.healthcaretreatmenttracking.util.SerializationUtils;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.CipherData;
import com.javierjordanluque.healthcaretreatmenttracking.util.security.SecurityService;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class MedicineRepository extends BaseRepository<Medicine> {
    private static final String TABLE_NAME_BASIC_MEDICINE = "MEDICINE";
    private final String NAME = "name";
    private final String NAME_IV = "name_iv";
    private final String NAME_HASH = "name_hash";
    private final String ACTIVE_SUBSTANCE = "active_substance";
    private final String ACTIVE_SUBSTANCE_IV = "active_substance_iv";
    private final String ACTIVE_SUBSTANCE_HASH = "active_substance_hash";
    private final String TABLE_NAME_MEDICINE = "TREATMENT_MEDICINE";
    private final String TREATMENT_ID = "treatment_id";
    private final String MEDICINE_ID = "medicine_id";
    private final String DOSE = "dose";
    private final String ADMINISTRATION_ROUTE = "administration_route";
    private final String INITIAL_DOSING_TIME = "initial_dosing_time";
    private final String DOSAGE_FREQUENCY_HOURS = "dosage_frequency_hours";
    private final String DOSAGE_FREQUENCY_MINUTES = "dosage_frequency_minutes";
    private Context context;

    public MedicineRepository(Context context) {
        super(TABLE_NAME_BASIC_MEDICINE, context);
        this.context = context;
    }

    @Override
    protected ContentValues getContentValues(Medicine medicine) {
        return null;
    }

    private ContentValues getBasicMedicineContentValues(Medicine medicine) {
        ContentValues contentValues = new ContentValues();

        CipherData cipherData;
        try {
            cipherData = SecurityService.encrypt(SerializationUtils.serialize(medicine.getName()));
            contentValues.put(NAME, cipherData.getEncryptedData());
            contentValues.put(NAME_IV, cipherData.getInitializationVector());
            contentValues.put(NAME_HASH, SecurityService.hash(SerializationUtils.serialize(medicine.getName())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cipherData = SecurityService.encrypt(SerializationUtils.serialize(medicine.getActiveSubstance()));
            contentValues.put(ACTIVE_SUBSTANCE, cipherData.getEncryptedData());
            contentValues.put(ACTIVE_SUBSTANCE_IV, cipherData.getInitializationVector());
            contentValues.put(ACTIVE_SUBSTANCE_HASH, SecurityService.hash(SerializationUtils.serialize(medicine.getActiveSubstance())));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentValues;
    }

    private ContentValues getMedicineContentValuesToInsert(Medicine medicine) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TREATMENT_ID, medicine.getTreatment().getId());
        contentValues.put(MEDICINE_ID, medicine.getId());
        if (medicine.getDose() != null)
            contentValues.put(DOSE, medicine.getDose());
        if (medicine.getAdministrationRoute() != null)
            contentValues.put(ADMINISTRATION_ROUTE, medicine.getAdministrationRoute().name());
        contentValues.put(INITIAL_DOSING_TIME, medicine.getInitialDosingTime().toInstant().getEpochSecond());
        contentValues.put(DOSAGE_FREQUENCY_HOURS, medicine.getDosageFrequencyHours());
        contentValues.put(DOSAGE_FREQUENCY_MINUTES, medicine.getDosageFrequencyMinutes());

        return contentValues;
    }

    private ContentValues getMedicineContentValuesToUpdate(Medicine medicine) {
        ContentValues contentValues = new ContentValues();

        if (medicine.getDose() != null)
            contentValues.put(DOSE, medicine.getDose());
        if (medicine.getAdministrationRoute() != null)
            contentValues.put(ADMINISTRATION_ROUTE, medicine.getAdministrationRoute().name());
        if (medicine.getInitialDosingTime() != null)
            contentValues.put(INITIAL_DOSING_TIME, medicine.getInitialDosingTime().toInstant().getEpochSecond());
        if (medicine.getDosageFrequencyHours() != null)
            contentValues.put(DOSAGE_FREQUENCY_HOURS, medicine.getDosageFrequencyHours());
        if (medicine.getDosageFrequencyMinutes() != null)
            contentValues.put(DOSAGE_FREQUENCY_MINUTES, medicine.getDosageFrequencyMinutes());

        return contentValues;
    }

    @Override
    protected Medicine cursorToItem(Cursor cursor) {
        return null;
    }

    @SuppressLint("Range")
    private BasicMedicine cursorToBasicMedicine(Cursor cursor) {
        CipherData cipherData = new CipherData(cursor.getBlob(cursor.getColumnIndex(NAME)), cursor.getBlob(cursor.getColumnIndex(NAME_IV)));
        String name = null;
        try {
            name = (String) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] activeSubstanceBytes = cursor.getBlob(cursor.getColumnIndex(ACTIVE_SUBSTANCE));
        byte[] activeSubstanceIV = cursor.getBlob(cursor.getColumnIndex(ACTIVE_SUBSTANCE_IV));
        String activeSubstance = null;
        if (activeSubstanceBytes != null && activeSubstanceIV != null) {
            cipherData = new CipherData(activeSubstanceBytes, activeSubstanceIV);
            try {
                activeSubstance = (String) SerializationUtils.deserialize(SecurityService.decrypt(cipherData), String.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BasicMedicine basicMedicine = new BasicMedicine(name, activeSubstance);
        basicMedicine.setId(cursor.getLong(cursor.getColumnIndex(ID)));

        return basicMedicine;
    }

    @SuppressLint("Range")
    private Medicine cursorToMedicine(Cursor cursor) {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        Treatment treatment = treatmentRepository.findById(cursor.getLong(cursor.getColumnIndex(TREATMENT_ID)));

        Integer dose = null;
        if (!cursor.isNull(cursor.getColumnIndex(DOSE)))
            dose = cursor.getInt(cursor.getColumnIndex(DOSE));

        AdministrationRoute administrationRoute = null;
        if (!cursor.isNull(cursor.getColumnIndex(ADMINISTRATION_ROUTE)))
            administrationRoute = AdministrationRoute.valueOf(cursor.getString(cursor.getColumnIndex(ADMINISTRATION_ROUTE)));

        Medicine medicine = new Medicine(null, treatment, null, null, dose, administrationRoute,
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(cursor.getLong(cursor.getColumnIndex(INITIAL_DOSING_TIME))),
                TimeZone.getDefault().toZoneId()), cursor.getInt(cursor.getColumnIndex(DOSAGE_FREQUENCY_HOURS)),
                cursor.getInt(cursor.getColumnIndex(DOSAGE_FREQUENCY_MINUTES)));
        medicine.setId(cursor.getLong(cursor.getColumnIndex(MEDICINE_ID)));

        return medicine;
    }

    @Override
    public long insert(Medicine medicine) {
        SQLiteDatabase db = open();

        long insertedId;
        BasicMedicine basicMedicine = findBasicMedicine(medicine);
        if (basicMedicine == null) {
            ContentValues values = getBasicMedicineContentValues(medicine);
            insertedId = db.insert(TABLE_NAME_BASIC_MEDICINE, null, values);
        } else {
            insertedId = basicMedicine.getId();
        }
        ContentValues values = getMedicineContentValuesToInsert(medicine);
        db.insert(TABLE_NAME_MEDICINE, null, values);

        close(db);
        return insertedId;
    }

    private BasicMedicine findBasicMedicine(Medicine medicine) {
        SQLiteDatabase db = open();
        Cursor cursor = null;

        try {
            byte[] nameHash = SecurityService.hash(SerializationUtils.serialize(medicine.getName()));
            byte[] activeSubstanceHash = SecurityService.hash(SerializationUtils.serialize(medicine.getActiveSubstance()));

            String selection = NAME_HASH + "=? and " + ACTIVE_SUBSTANCE_HASH + "=?";
            String[] selectionArgs = {new String(nameHash, StandardCharsets.UTF_8), new String(activeSubstanceHash, StandardCharsets.UTF_8)};
            cursor = db.query(TABLE_NAME_BASIC_MEDICINE, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToBasicMedicine(cursor);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return null;
    }

    @Override
    public void update(Medicine medicine) {
        SQLiteDatabase db = open();

        try {
            ContentValues values = getMedicineContentValuesToUpdate(medicine);
            long treatmentId = medicine.getTreatment().getId();
            long medicineId = medicine.getId();
            String selection = TREATMENT_ID + "=? and " + MEDICINE_ID + "=?";
            String[] selectionArgs = {String.valueOf(treatmentId), String.valueOf(medicineId)};
            db.update(TABLE_NAME_MEDICINE, values, selection, selectionArgs);
        } finally {
            close(db);
        }
    }

    @Override
    public void delete(Medicine medicine) {
        SQLiteDatabase db = open();

        try {
            long treatmentId = medicine.getTreatment().getId();
            long medicineId = medicine.getId();
            String selection = TREATMENT_ID + "=? and " + MEDICINE_ID + "=?";
            String[] selectionArgs = {String.valueOf(treatmentId), String.valueOf(medicineId)};
            db.delete(TABLE_NAME_MEDICINE, selection, selectionArgs);

            if (!hasTreatment(medicine)) {
                selection = ID + "=?";
                selectionArgs = new String[]{String.valueOf(medicineId)};
                db.delete(TABLE_NAME_BASIC_MEDICINE, selection, selectionArgs);
            }
        } finally {
            close(db);
        }
    }

    private boolean hasTreatment(Medicine medicine) {
        SQLiteDatabase db = open();
        Cursor cursor = null;

        try {
            String[] projection = {"COUNT(*)"};
            String selection = MEDICINE_ID + "=?";
            String[] selectionArgs = {String.valueOf(medicine.getId())};
            cursor = db.query(TABLE_NAME_MEDICINE, projection, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);

                return count > 0;
            }
        } finally {
            if (cursor != null)
                cursor.close();
            db.close();
        }

        return false;
    }

    public Medicine findById(long treatmentId ,long medicineId) {
        SQLiteDatabase db = open();
        Cursor cursor = null;

        try {
            String selection = ID + "=?";
            String[] selectionArgs = {String.valueOf(medicineId)};
            cursor = db.query(TABLE_NAME_BASIC_MEDICINE, null, selection, selectionArgs, null, null, null);

            BasicMedicine basicMedicine;
            if (cursor != null && cursor.moveToFirst()) {
                basicMedicine = cursorToBasicMedicine(cursor);

                selection = TREATMENT_ID + "=? and " + MEDICINE_ID + "=?";
                selectionArgs =  new String[]{String.valueOf(treatmentId), String.valueOf(medicineId)};
                cursor = db.query(TABLE_NAME_MEDICINE, null, selection, selectionArgs, null, null, null);

                Medicine medicine;
                if (cursor != null && cursor.moveToFirst()) {
                    medicine = cursorToMedicine(cursor);

                    medicine.setName(basicMedicine.getName());
                    medicine.setActiveSubstance(basicMedicine.getActiveSubstance());

                    return medicine;
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return null;
    }

    @Override
    public List<Medicine> findAll() {
        List<BasicMedicine> basicMedicines = new ArrayList<>();
        List<Medicine> medicines = new ArrayList<>();
        SQLiteDatabase db = open();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_NAME_BASIC_MEDICINE, null, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    BasicMedicine basicMedicine = cursorToBasicMedicine(cursor);
                    basicMedicines.add(basicMedicine);
                }

                cursor = db.query(TABLE_NAME_MEDICINE, null, null, null, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        Medicine medicine = cursorToMedicine(cursor);
                        for (BasicMedicine basicMedicine : basicMedicines) {
                            if (medicine.getId() == basicMedicine.getId()) {
                                medicine.setName(basicMedicine.getName());
                                medicine.setActiveSubstance(basicMedicine.getActiveSubstance());
                            }
                        }
                        medicines.add(medicine);
                    }
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return medicines;
    }

    public List<Medicine> findTreatmentMedicines(long treatmentId) {
        List<Medicine> medicines = new ArrayList<>();
        SQLiteDatabase db = open();
        Cursor cursor = null;

        try {
            String selection = TREATMENT_ID + "=?";
            String[] selectionArgs = {String.valueOf(treatmentId)};
            cursor = db.query(TABLE_NAME_MEDICINE, null, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Medicine medicine = cursorToMedicine(cursor);
                    medicines.add(medicine);
                }

                for (Medicine medicine : medicines) {
                    selection = ID + "=?";
                    selectionArgs = new String[]{String.valueOf(medicine.getId())};
                    cursor = db.query(TABLE_NAME_BASIC_MEDICINE, null, selection, selectionArgs, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        BasicMedicine basicMedicine = cursorToBasicMedicine(cursor);
                        medicine.setName(basicMedicine.getName());
                        medicine.setActiveSubstance(basicMedicine.getActiveSubstance());
                    }
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
            close(db);
        }

        return medicines;
    }
}
