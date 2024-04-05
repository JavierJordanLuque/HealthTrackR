package com.javierjordanluque.healthtrackr.models.enumerations;

import android.os.Parcel;
import android.os.Parcelable;

public enum BloodType implements Parcelable {
    A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE, AB_POSITIVE, AB_NEGATIVE, O_POSITIVE, O_NEGATIVE;

    public static final Creator<BloodType> CREATOR = new Creator<BloodType>() {
        @Override
        public BloodType createFromParcel(Parcel in) {
            return BloodType.values()[in.readInt()];
        }

        @Override
        public BloodType[] newArray(int size) {
            return new BloodType[size];
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
