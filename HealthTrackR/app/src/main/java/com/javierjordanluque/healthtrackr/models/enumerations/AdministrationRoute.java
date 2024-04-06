package com.javierjordanluque.healthtrackr.models.enumerations;

import android.os.Parcel;
import android.os.Parcelable;

public enum AdministrationRoute implements Parcelable {
    ORAL, TOPICAL, PARENTERAL, INHALATION, OPHTHALMIC, OTIC, NASAL, RECTAL, UNSPECIFIED;

    public static final Creator<AdministrationRoute> CREATOR = new Creator<AdministrationRoute>() {
        @Override
        public AdministrationRoute createFromParcel(Parcel in) {
            return AdministrationRoute.values()[in.readInt()];
        }

        @Override
        public AdministrationRoute[] newArray(int size) {
            return new AdministrationRoute[size];
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
