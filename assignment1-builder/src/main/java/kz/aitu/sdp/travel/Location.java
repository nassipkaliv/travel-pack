package kz.aitu.sdp.travel;

public record Location(String city, String country) {
    public Location {
        if (city == null || city.isBlank() || country == null || country.isBlank()) {
            throw new IllegalArgumentException("City and country must not be blank");
        }
    }
}
