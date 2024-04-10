package com.javierjordanluque.healthtrackr.models;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.models.enumerations.MultimediaType;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

import java.util.Objects;

public class Multimedia implements Identifiable, Parcelable {
    private long id;
    private Step step;
    private final MultimediaType type;
    private final String path;

    public Multimedia(Context context, Step step, MultimediaType type, String path) throws DBInsertException {
        this.step = step;
        this.type = type;
        this.path = path;

        if (context != null)
            this.step.addMultimedia(context, this);
    }

    public Object getMedia() {
        switch (type) {
            case IMAGE:
                return BitmapFactory.decodeFile(path);
            case VIDEO:
                return Uri.parse(path);
            default:
                return null;
        }
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public MultimediaType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Multimedia multimedia = (Multimedia) obj;
        return id == multimedia.id &&
                Objects.equals(step, multimedia.step) &&
                type == multimedia.type &&
                Objects.equals(path, multimedia.path);
    }

    protected Multimedia(Parcel in) {
        id = in.readLong();
        type = in.readParcelable(MultimediaType.class.getClassLoader());
        path = in.readString();
    }

    public static final Creator<Multimedia> CREATOR = new Creator<Multimedia>() {
        @Override
        public Multimedia createFromParcel(Parcel in) {
            return new Multimedia(in);
        }

        @Override
        public Multimedia[] newArray(int size) {
            return new Multimedia[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeParcelable(type, flags);
        dest.writeString(path);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
