package com.javierjordanluque.healthtrackr.util.media;

import android.net.Uri;

/** @noinspection unused*/
public class Media {
    private final Uri uri;
    private final String name;
    private final long size;
    private final String mimeType;
    private final Long duration;

    public Media(Uri uri, String name, long size, String mimeType, Long duration) {
        this.uri = uri;
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
        this.duration = duration;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getDuration() {
        return duration;
    }
}
