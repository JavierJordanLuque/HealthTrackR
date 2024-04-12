package com.javierjordanluque.healthtrackr.models;

public class Allergy implements Identifiable {
    private long id;
    private final User user;
    private final String name;

    public Allergy(User user, String name) {
        this.user = user;
        this.name = name;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Allergy allergy = (Allergy) obj;
        return user.equals(allergy.user) && name.equals(allergy.name);
    }
}
