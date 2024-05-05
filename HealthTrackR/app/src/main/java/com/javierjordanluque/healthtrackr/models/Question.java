package com.javierjordanluque.healthtrackr.models;

import android.content.Context;

import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

import java.util.Objects;

public class Question implements Identifiable {
    private long id;
    private Treatment treatment;
    private final String description;

    public Question(Context context, Treatment treatment, String description) throws DBInsertException {
        this.treatment = treatment;
        this.description = description;

        if (context != null)
            this.treatment.addQuestion(context, this);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Question question = (Question) obj;
        return id == question.id &&
                Objects.equals(treatment, question.treatment) &&
                Objects.equals(description, question.description);
    }
}
