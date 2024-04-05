package com.javierjordanluque.healthtrackr.models;

import android.os.Parcel;
import android.os.Parcelable;

public class PreviousMedicalCondition implements Identifiable, Parcelable {
    private long id;
    private final User user;
    private final String name;

    public PreviousMedicalCondition(User user, String name) {
        this.user = user;
        this.name = name;

        user.addCondition(this);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    protected PreviousMedicalCondition(Parcel in) {
        id = in.readLong();
        user = in.readParcelable(User.class.getClassLoader());
        name = in.readString();
    }

    public static final Creator<PreviousMedicalCondition> CREATOR = new Creator<PreviousMedicalCondition>() {
        @Override
        public PreviousMedicalCondition createFromParcel(Parcel in) {
            return new PreviousMedicalCondition(in);
        }

        @Override
        public PreviousMedicalCondition[] newArray(int size) {
            return new PreviousMedicalCondition[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeParcelable(user, flags);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
