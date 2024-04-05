package com.javierjordanluque.healthtrackr.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

public class Symptom implements Identifiable, Parcelable {
    private long id;
    private final Treatment treatment;
    private final String description;

    public Symptom(Context context, Treatment treatment, String description) throws DBInsertException {
        this.treatment = treatment;
        this.description = description;

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

    public String getDescription() {
        return description;
    }

    protected Symptom(Parcel in) {
        id = in.readLong();
        treatment = in.readParcelable(Treatment.class.getClassLoader());
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
        dest.writeParcelable(treatment, flags);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
