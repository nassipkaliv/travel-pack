package kz.aitu.sdp.trip.booking;

/** Thrown when the trip and the customer do not fit together. */
public class BookingRejectedException extends RuntimeException {

    public BookingRejectedException(String message) {
        super(message);
    }
}
