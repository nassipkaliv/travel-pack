package kz.aitu.sdp.trip.naive;

/** Product type 3: what the traveler does at the destination. */
public interface Excursion {

    String describe();

    String guide();

    int groupSize();

    int pricePerPerson();
}
