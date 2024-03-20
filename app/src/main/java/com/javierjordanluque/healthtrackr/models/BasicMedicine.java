package com.javierjordanluque.healthtrackr.models;

public class BasicMedicine {
    private long id;
    private final String name;
    private final String activeSubstance;

    public BasicMedicine(String name, String activeSubstance) {
        this.name = name;
        this.activeSubstance = activeSubstance;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getActiveSubstance() {
        return activeSubstance;
    }
}
