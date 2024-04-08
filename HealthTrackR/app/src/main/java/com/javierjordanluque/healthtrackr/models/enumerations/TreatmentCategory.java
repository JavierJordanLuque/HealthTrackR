package com.javierjordanluque.healthtrackr.models.enumerations;

import android.os.Parcel;
import android.os.Parcelable;

public enum TreatmentCategory implements Parcelable {
    MEDICAL, PHARMACOLOGICAL, PHYSIOTHERAPY, REHABILITATION, PSYCHOLOGICAL, PREVENTIVE, CHRONIC, ALTERNATIVE;

    public static final Creator<TreatmentCategory> CREATOR = new Creator<TreatmentCategory>() {
        @Override
        public TreatmentCategory createFromParcel(Parcel in) {
            return TreatmentCategory.values()[in.readInt()];
        }

        @Override
        public TreatmentCategory[] newArray(int size) {
            return new TreatmentCategory[size];
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
