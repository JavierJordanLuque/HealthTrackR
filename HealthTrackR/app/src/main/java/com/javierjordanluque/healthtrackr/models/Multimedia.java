package com.javierjordanluque.healthtrackr.models;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.javierjordanluque.healthtrackr.models.enumerations.MultimediaType;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

import java.util.Objects;

public class Multimedia implements Identifiable {
    private long id;
    private final Step step;
    private final MultimediaType type;
    private final String path;

    public Multimedia(Context context, Step step, MultimediaType type, String path) throws DBInsertException {
        this.step = step;
        this.type = type;
        this.path = path;

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
}
