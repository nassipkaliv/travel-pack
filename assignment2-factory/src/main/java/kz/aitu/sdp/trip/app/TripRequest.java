package kz.aitu.sdp.trip.app;

/** What the traveler asks for. Independent of the product family. */
public record TripRequest(String travelerName, String city, int nights, int travelers) {

    public TripRequest {
        if (travelerName == null || travelerName.isBlank()) {
            throw new IllegalArgumentException("Traveler name must not be blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City must not be blank");
        }
        if (nights < 1) {
            throw new IllegalArgumentException("Trip must last at least one night, got " + nights);
        }
        if (travelers < 1) {
            throw new IllegalArgumentException("Trip needs at least one traveler, got " + travelers);
        }
    }
}
