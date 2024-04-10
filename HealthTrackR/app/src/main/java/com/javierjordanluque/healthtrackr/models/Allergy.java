package com.javierjordanluque.healthtrackr.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Allergy implements Identifiable, Parcelable {
    private long id;
    private User user;
    private final String name;

    public Allergy(User user, String name) {
        this.user = user;
        this.name = name;
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

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Allergy allergy = (Allergy) obj;
        return user.equals(allergy.user) && name.equals(allergy.name);
    }

    protected Allergy(Parcel in) {
        id = in.readLong();
        name = in.readString();
    }

    public static final Parcelable.Creator<Allergy> CREATOR = new Parcelable.Creator<Allergy>() {
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
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
