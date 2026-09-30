package kz.aitu.sdp.trip.product;

/** Product type 1: how the traveler reaches the destination. */
public interface Transport extends TripComponent {

    /** Door-to-door duration in minutes. Drives the arrival time of the whole trip. */
    int travelTimeMinutes();

    /** Baggage allowance per person. Drives the packing advice. */
    int baggageKg();

    /** True when the traveler is picked up at home and dropped at the hotel. */
    boolean doorToDoor();

    int pricePerPerson();
}
