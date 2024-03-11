package com.javierjordanluque.healthcaretreatmenttracking.models;

public class Allergy implements Identifiable {
    private long id;
    private final User user;
    private final String name;

    public Allergy(User user, String name) {
        this.user = user;
        this.name = name;

        this.user.addAllergy(this);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }
}
