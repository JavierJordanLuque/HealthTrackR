package com.javierjordanluque.healthtrackr.models;

import android.content.Context;

import com.javierjordanluque.healthtrackr.db.repositories.MultimediaRepository;
import com.javierjordanluque.healthtrackr.db.repositories.StepRepository;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;

import java.util.List;
import java.util.Objects;

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

        if (context != null)
            this.treatment.addStep(context, this);
    }

    private Step(){
    }

    public void modifyStep(Context context, String title, String description, int numOrder) throws DBUpdateException, DBFindException {
        Step step = new Step();
        step.setId(this.id);

        if (!this.title.equals(title)) {
            setTitle(title);
            step.setTitle(this.title);
        }

        if ((this.description == null && description != null ) || (description != null && !this.description.equals(description))) {
            setDescription(description);
            step.setDescription(description);
        } else if (this.description != null && description == null) {
            setDescription(null);
            step.setDescription("");
        }

        if (this.numOrder != numOrder) {
            if (numOrder < this.numOrder) {
                for (Step otherStep : step.getTreatment().getSteps(context)) {
                    if (!Objects.equals(this.numOrder, otherStep.getNumOrder()) && otherStep.getNumOrder() >= numOrder && otherStep.getNumOrder() < this.numOrder) {
                        Step newOtherStep = new Step();
                        newOtherStep.setId(otherStep.getId());

                        otherStep.setNumOrder(otherStep.getNumOrder() + 1);
                        newOtherStep.setNumOrder(otherStep.getNumOrder() + 1);

                        StepRepository stepRepository = new StepRepository(context);
                        stepRepository.update(newOtherStep);
                    }
                }
            } else {
                for (Step otherStep : step.getTreatment().getSteps(context)) {
                    if (!Objects.equals(this.numOrder, otherStep.getNumOrder()) && otherStep.getNumOrder() <= numOrder && otherStep.getNumOrder() > this.numOrder) {
                        Step newOtherStep = new Step();
                        newOtherStep.setId(otherStep.getId());

                        otherStep.setNumOrder(otherStep.getNumOrder() - 1);
                        newOtherStep.setNumOrder(otherStep.getNumOrder() - 1);

                        StepRepository stepRepository = new StepRepository(context);
                        stepRepository.update(newOtherStep);
                    }
                }
            }
            setNumOrder(numOrder);
            step.setNumOrder(numOrder);
        }

        StepRepository stepRepository = new StepRepository(context);
        stepRepository.update(step);
    }

    protected void addMultimedia(Context context, Multimedia multimedia) throws DBInsertException {
        MultimediaRepository multimediaRepository = new MultimediaRepository(context);
        multimedia.setId(multimediaRepository.insert(multimedia));
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Step step = (Step) obj;
        return id == step.id &&
                Objects.equals(treatment, step.treatment) &&
                Objects.equals(title, step.title) &&
                Objects.equals(description, step.description) &&
                Objects.equals(numOrder, step.numOrder);
    }
}
