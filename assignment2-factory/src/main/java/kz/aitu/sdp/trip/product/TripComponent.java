package kz.aitu.sdp.trip.product;

/** Anything a trip is assembled from. Every component knows which family it belongs to. */
public interface TripComponent {

    TripTier tier();

    String describe();
}
