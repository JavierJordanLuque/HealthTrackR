package com.javierjordanluque.healthtrackr.models;

import java.util.Objects;

public class Location {
    private final String place;
    private final Double latitude;
    private final Double longitude;

    public Location(String place, Double latitude, Double longitude) {
        this.place = place;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlace() {
        return place;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        Location location = (Location) object;
        return Objects.equals(place, location.place) && Objects.equals(latitude, location.latitude) && Objects.equals(longitude, location.longitude);
    }
}
