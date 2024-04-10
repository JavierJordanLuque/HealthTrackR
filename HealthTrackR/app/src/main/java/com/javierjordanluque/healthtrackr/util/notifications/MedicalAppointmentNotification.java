package com.javierjordanluque.healthtrackr.util.notifications;

import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.models.MedicalAppointment;

public class MedicalAppointmentNotification extends Notification implements Parcelable {
    private MedicalAppointment appointment;

    public MedicalAppointmentNotification(MedicalAppointment appointment, long timestamp) {
        super(timestamp);
        this.appointment = appointment;
    }

    public MedicalAppointment getAppointment() {
        return appointment;
    }

    public void setAppointment(MedicalAppointment appointment) {
        this.appointment = appointment;
    }

    protected MedicalAppointmentNotification(Parcel in) {
        super(in.readLong());
        setId(in.readLong());
    }

    public static final Creator<MedicalAppointmentNotification> CREATOR = new Creator<MedicalAppointmentNotification>() {
        @Override
        public MedicalAppointmentNotification createFromParcel(Parcel in) {
            return new MedicalAppointmentNotification(in);
        }

        @Override
        public MedicalAppointmentNotification[] newArray(int size) {
            return new MedicalAppointmentNotification[size];
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
