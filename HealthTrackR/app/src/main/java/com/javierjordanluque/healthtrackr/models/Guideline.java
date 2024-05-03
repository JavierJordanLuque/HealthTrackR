package com.javierjordanluque.healthtrackr.models;

import android.content.Context;

import com.javierjordanluque.healthtrackr.db.repositories.MultimediaRepository;
import com.javierjordanluque.healthtrackr.db.repositories.GuidelineRepository;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;

import java.util.List;
import java.util.Objects;

public class Guideline implements Identifiable {
    private long id;
    private Treatment treatment;
    private String title;
    private String description;
    private Integer numOrder;
    private List<Multimedia> multimedias;

    public Guideline(Context context, Treatment treatment, String title, String description, int numOrder) throws DBInsertException, DBFindException, DBUpdateException {
        this.treatment = treatment;
        this.title = title;
        this.description = description;
        this.numOrder = numOrder;

        if (context != null)
            this.treatment.addGuideline(context, this);
    }

    private Guideline(){
    }

    public void modifyGuideline(Context context, String title, String description, int numOrder) throws DBUpdateException, DBFindException {
        Guideline guideline = new Guideline();
        guideline.setId(this.id);

        if (!this.title.equals(title)) {
            setTitle(title);
            guideline.setTitle(this.title);
        }

        if ((this.description == null && description != null ) || (description != null && !this.description.equals(description))) {
            setDescription(description);
            guideline.setDescription(description);
        } else if (this.description != null && description == null) {
            setDescription(null);
            guideline.setDescription("");
        }

        if (this.numOrder != numOrder) {
            GuidelineRepository guidelineRepository = new GuidelineRepository(context);
            if (numOrder < this.numOrder) {
                for (Guideline otherGuideline : guideline.getTreatment().getGuidelines(context)) {
                    if (!Objects.equals(this.numOrder, otherGuideline.getNumOrder()) && otherGuideline.getNumOrder() >= numOrder && otherGuideline.getNumOrder() < this.numOrder) {
                        Guideline newOtherGuideline = new Guideline();
                        newOtherGuideline.setId(otherGuideline.getId());

                        otherGuideline.setNumOrder(otherGuideline.getNumOrder() + 1);
                        newOtherGuideline.setNumOrder(otherGuideline.getNumOrder() + 1);

                        guidelineRepository.update(newOtherGuideline);
                    }
                }
            } else {
                for (Guideline otherGuideline : guideline.getTreatment().getGuidelines(context)) {
                    if (!Objects.equals(this.numOrder, otherGuideline.getNumOrder()) && otherGuideline.getNumOrder() <= numOrder && otherGuideline.getNumOrder() > this.numOrder) {
                        Guideline newOtherGuideline = new Guideline();
                        newOtherGuideline.setId(otherGuideline.getId());

                        otherGuideline.setNumOrder(otherGuideline.getNumOrder() - 1);
                        newOtherGuideline.setNumOrder(otherGuideline.getNumOrder() - 1);

                        guidelineRepository.update(newOtherGuideline);
                    }
                }
            }
            setNumOrder(numOrder);
            guideline.setNumOrder(numOrder);
        }

        if (!(guideline.getTitle() == null && guideline.getDescription() == null && guideline.getNumOrder() == null)) {
            GuidelineRepository guidelineRepository = new GuidelineRepository(context);
            guidelineRepository.update(guideline);
        }
    }

    public void adjustGuidelinesNumOrder(Context context, boolean increase) throws DBUpdateException, DBFindException {
        GuidelineRepository guidelineRepository = new GuidelineRepository(context);

        int shift = increase ? 1 : -1;

        for (Guideline guideline : this.treatment.getGuidelines(context)) {
            if ((increase && guideline.getNumOrder() >= this.getNumOrder()) || (!increase && guideline.getNumOrder() > this.getNumOrder())) {
                Guideline guidelineToUpdate = new Guideline();
                guidelineToUpdate.setId(guideline.getId());

                guideline.setNumOrder(guideline.getNumOrder() + shift);
                guidelineToUpdate.setNumOrder(guideline.getNumOrder() + shift);
                guidelineRepository.update(guidelineToUpdate);
            }
        }
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
            setMultimedias(multimediaRepository.findGuidelineMultimedias(this.id));
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
        Guideline guideline = (Guideline) obj;
        return id == guideline.id &&
                Objects.equals(treatment, guideline.treatment) &&
                Objects.equals(title, guideline.title) &&
                Objects.equals(description, guideline.description) &&
                Objects.equals(numOrder, guideline.numOrder);
    }
}
