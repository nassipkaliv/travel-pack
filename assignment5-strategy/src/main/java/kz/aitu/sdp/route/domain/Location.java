package kz.aitu.sdp.route.domain;

import java.util.Objects;

/** A named point on the map: a warehouse, a hub or a customer address. */
public record Location(String name, double latitude, double longitude) {

    public Location {
        Objects.requireNonNull(name, "location name is required");
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude out of range: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude out of range: " + longitude);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
