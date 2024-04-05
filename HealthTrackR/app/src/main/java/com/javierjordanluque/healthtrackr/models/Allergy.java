package com.javierjordanluque.healthtrackr.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Allergy implements Identifiable, Parcelable {
    private long id;
    private final User user;
    private final String name;

    public Allergy(User user, String name) {
        this.user = user;
        this.name = name;

        this.user.addAllergy(this);
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

    protected Allergy(Parcel in) {
        id = in.readLong();
        user = in.readParcelable(User.class.getClassLoader());
        name = in.readString();
    }

    public static final Creator<Allergy> CREATOR = new Creator<Allergy>() {
        @Override
        public Allergy createFromParcel(Parcel in) {
            return new Allergy(in);
        }

        @Override
        public Allergy[] newArray(int size) {
            return new Allergy[size];
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
