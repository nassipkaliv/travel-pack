package kz.aitu.sdp.trip.naive;

/** Product type 1: how the traveler gets to the destination. */
public interface Transport {

    String describe();

    int travelTimeMinutes();

    int baggageKg();

    int pricePerPerson();
}
