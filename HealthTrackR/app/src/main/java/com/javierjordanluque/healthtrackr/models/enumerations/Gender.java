package com.javierjordanluque.healthtrackr.models.enumerations;

import android.os.Parcel;
import android.os.Parcelable;

public enum Gender implements Parcelable {
    MALE, FEMALE, UNSPECIFIED;

    public static final Creator<Gender> CREATOR = new Creator<Gender>() {
        @Override
        public Gender createFromParcel(Parcel in) {
            return Gender.values()[in.readInt()];
        }

        @Override
        public Gender[] newArray(int size) {
            return new Gender[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
