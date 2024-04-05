package com.javierjordanluque.healthtrackr.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

public class Question implements Identifiable, Parcelable {
    private long id;
    private final Treatment treatment;
    private final String description;

    public Question(Context context, Treatment treatment, String description) throws DBInsertException {
        this.treatment = treatment;
        this.description = description;

        this.treatment.addQuestion(context, this);
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

    protected Question(Parcel in) {
        id = in.readLong();
        treatment = in.readParcelable(Treatment.class.getClassLoader());
        description = in.readString();
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
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
