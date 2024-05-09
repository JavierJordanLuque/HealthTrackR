package com.javierjordanluque.healthtrackr.models;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.javierjordanluque.healthtrackr.models.enumerations.MultimediaType;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

import java.util.Objects;

public class Multimedia implements Identifiable {
    private long id;
    private final Guideline guideline;
    private final MultimediaType type;
    private final Uri uri;

    public Multimedia(Context context, Guideline guideline, MultimediaType type, Uri uri) throws DBInsertException {
        this.guideline = guideline;
        this.type = type;
        this.uri = uri;

        if (context != null)
            this.guideline.addMultimedia(context, this);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Guideline getGuideline() {
        return guideline;
    }

    public MultimediaType getType() {
        return type;
    }

    public Uri getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Multimedia multimedia = (Multimedia) obj;
        return id == multimedia.id &&
                Objects.equals(guideline, multimedia.guideline) &&
                type == multimedia.type &&
                Objects.equals(uri, multimedia.uri);
    }
}
