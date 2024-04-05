package com.javierjordanluque.healthtrackr.models.enumerations;

import android.os.Parcel;
import android.os.Parcelable;

public enum MultimediaType implements Parcelable {
    IMAGE, VIDEO;

    public static final Creator<MultimediaType> CREATOR = new Creator<MultimediaType>() {
        @Override
        public MultimediaType createFromParcel(Parcel in) {
            return MultimediaType.values()[in.readInt()];
        }

        @Override
        public MultimediaType[] newArray(int size) {
            return new MultimediaType[size];
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
