package kz.aitu.sdp.trip.naive;

/** Product type 2: where the traveler sleeps. */
public interface Accommodation {

    String describe();

    boolean hasPrivateRoom();

    String mealsIncluded();

    int pricePerNight();
}
