package com.javierjordanluque.healthcaretreatmenttracking.models;

import android.content.Context;

import com.javierjordanluque.healthcaretreatmenttracking.models.enumerations.MultimediaType;

public class Multimedia implements Identifiable {
    private long id;
    private Step step;
    private MultimediaType type;
    private String path;

    public Multimedia(Context context, Step step, MultimediaType type, String path) {
        this.step = step;
        this.type = type;
        this.path = path;
        this.step.addMultimedia(context, this);
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
}
