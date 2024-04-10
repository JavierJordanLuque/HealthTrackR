package com.javierjordanluque.healthtrackr.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

import java.util.Objects;

public class Question implements Identifiable, Parcelable {
    private long id;
    private Treatment treatment;
    private final String description;

    public Question(Context context, Treatment treatment, String description) throws DBInsertException {
        this.treatment = treatment;
        this.description = description;

        if (context != null)
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
        Question question = (Question) obj;
        return id == question.id &&
                Objects.equals(treatment, question.treatment) &&
                Objects.equals(description, question.description);
    }

    protected Question(Parcel in) {
        id = in.readLong();
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
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
