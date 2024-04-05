package com.javierjordanluque.healthtrackr.util.notifications;

import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.models.Identifiable;

public abstract class Notification implements Identifiable, Parcelable {
    private long id;
    private final long timestamp;

    public Notification(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    protected Notification(Parcel in) {
        id = in.readLong();
        timestamp = in.readLong();
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in) {
                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    // Not needed as abstract class
                }

                @Override
                public int describeContents() {
                    return 0;
                }
            };
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
