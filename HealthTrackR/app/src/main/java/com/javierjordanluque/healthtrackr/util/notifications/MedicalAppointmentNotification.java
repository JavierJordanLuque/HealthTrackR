package com.javierjordanluque.healthtrackr.util.notifications;

import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.models.MedicalAppointment;

public class MedicalAppointmentNotification extends Notification {
    private final MedicalAppointment appointment;

    public MedicalAppointmentNotification(MedicalAppointment appointment, long timestamp) {
        super(timestamp);
        this.appointment = appointment;
    }

    public MedicalAppointment getAppointment() {
        return appointment;
    }

    protected MedicalAppointmentNotification(Parcel in) {
        super(in);
        appointment = in.readParcelable(MedicalAppointment.class.getClassLoader());
    }

    public static final Parcelable.Creator<MedicalAppointmentNotification> CREATOR = new Parcelable.Creator<MedicalAppointmentNotification>() {
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
        super.writeToParcel(dest, flags);
        dest.writeParcelable(appointment, flags);
    }
}
