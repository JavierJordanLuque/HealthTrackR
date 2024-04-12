package com.javierjordanluque.healthtrackr.models;

public class Location {
    private final double latitude;
    private final double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Location location = (Location) obj;
        return Double.compare(location.latitude, latitude) == 0 &&
                Double.compare(location.longitude, longitude) == 0;
    }
}
