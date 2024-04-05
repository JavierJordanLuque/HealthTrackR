package com.javierjordanluque.healthtrackr.util.notifications;

import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.models.Medicine;

public class MedicationNotification extends Notification {
    private final Medicine medicine;

    public MedicationNotification(Medicine medicine, long timestamp) {
        super(timestamp);
        this.medicine = medicine;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    protected MedicationNotification(Parcel in) {
        super(in);
        medicine = in.readParcelable(Medicine.class.getClassLoader());
    }

    public static final Parcelable.Creator<MedicationNotification> CREATOR = new Parcelable.Creator<MedicationNotification>() {
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
        super.writeToParcel(dest, flags);
        dest.writeParcelable(medicine, flags);
    }
}
