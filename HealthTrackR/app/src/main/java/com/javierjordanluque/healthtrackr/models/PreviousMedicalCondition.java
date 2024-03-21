package com.javierjordanluque.healthtrackr.models;

public class PreviousMedicalCondition implements Identifiable {
    private long id;
    private final User user;
    private final String name;

    public PreviousMedicalCondition(User user, String name) {
        this.user = user;
        this.name = name;

        user.addCondition(this);
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