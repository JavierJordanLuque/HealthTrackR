package com.javierjordanluque.healthtrackr.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

import java.util.Objects;

public class Symptom implements Identifiable, Parcelable {
    private long id;
    private Treatment treatment;
    private final String description;

    public Symptom(Context context, Treatment treatment, String description) throws DBInsertException {
        this.treatment = treatment;
        this.description = description;

        if (context != null)
            this.treatment.addSymptom(context, this);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Symptom symptom = (Symptom) obj;
        return id == symptom.id &&
                Objects.equals(treatment, symptom.treatment) &&
                Objects.equals(description, symptom.description);
    }

    protected Symptom(Parcel in) {
        id = in.readLong();
        description = in.readString();
    }

    public static final Creator<Symptom> CREATOR = new Creator<Symptom>() {
        @Override
        public Symptom createFromParcel(Parcel in) {
            return new Symptom(in);
        }

        @Override
        public Symptom[] newArray(int size) {
            return new Symptom[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
