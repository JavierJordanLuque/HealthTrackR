package com.javierjordanluque.healthtrackr.util.notifications;

import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.models.Medicine;

public class MedicationNotification extends Notification implements Parcelable {
    private Medicine medicine;

    public MedicationNotification(Medicine medicine, long timestamp) {
        super(timestamp);
        this.medicine = medicine;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    protected MedicationNotification(Parcel in) {
        super(in.readLong());
        setId(in.readLong());
    }

    public static final Creator<MedicationNotification> CREATOR = new Creator<MedicationNotification>() {
        @Override
        public MedicationNotification createFromParcel(Parcel in) {
            return new MedicationNotification(in);
        }

        @Override
        public MedicationNotification[] newArray(int size) {
            return new MedicationNotification[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getTimestamp());
        dest.writeLong(getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
