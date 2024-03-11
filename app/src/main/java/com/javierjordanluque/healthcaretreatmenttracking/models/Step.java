package com.javierjordanluque.healthcaretreatmenttracking.models;

import android.content.Context;

import com.javierjordanluque.healthcaretreatmenttracking.db.repositories.MultimediaRepository;
import com.javierjordanluque.healthcaretreatmenttracking.db.repositories.StepRepository;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBFindException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBInsertException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBUpdateException;

import java.util.List;

public class Step implements Identifiable {
    private long id;
    private Treatment treatment;
    private String title;
    private String description;
    private Integer numOrder;
    private List<Multimedia> multimedias;

    public Step(Context context, Treatment treatment, String title, String description, int numOrder) throws DBInsertException {
        this.treatment = treatment;
        this.title = title;
        this.description = description;
        this.numOrder = numOrder;

        this.treatment.addStep(context, this);
    }

    private Step(){
    }

    public void modifyStep(Context context, String title, String description, int numOrder) throws DBUpdateException {
        Step step = new Step();
        step.setId(this.id);

        if (!this.title.equals(title)) {
            setTitle(title);
            step.setTitle(this.title);
        }
        if ((this.description == null && description != null ) || (description != null && !this.description.equals(description))) {
            setDescription(description);
            step.setDescription(description);
        }
        if (this.numOrder != numOrder) {
            setNumOrder(numOrder);
            step.setNumOrder(numOrder);
        }

        StepRepository stepRepository = new StepRepository(context);
        stepRepository.update(step);
    }

    protected void addMultimedia(Context context, Multimedia multimedia) throws DBInsertException {
        if (context != null) {
            MultimediaRepository multimediaRepository = new MultimediaRepository(context);
            multimedia.setId(multimediaRepository.insert(multimedia));
        }
        multimedias.add(multimedia);
    }

    public void removeMultimedia(Context context, Multimedia multimedia) throws DBDeleteException {
        MultimediaRepository multimediaRepository = new MultimediaRepository(context);
        multimediaRepository.delete(multimedia);
        multimedias.remove(multimedia);
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

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public Integer getNumOrder() {
        return numOrder;
    }

    private void setNumOrder(int numOrder) {
        this.numOrder = numOrder;
    }

    public List<Multimedia> getMultimedias(Context context) throws DBFindException {
        if (multimedias == null) {
            MultimediaRepository multimediaRepository = new MultimediaRepository(context);
            setMultimedias(multimediaRepository.findStepMultimedias(this.id));
        }

        return multimedias;
    }

    private void setMultimedias(List<Multimedia> multimedias) {
        this.multimedias = multimedias;
    }
}
