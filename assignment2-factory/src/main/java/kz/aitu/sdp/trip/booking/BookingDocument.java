package kz.aitu.sdp.trip.booking;

/**
 * Product of the Factory Method: the document a traveler receives after booking.
 * Each booking channel issues its own kind.
 */
public interface BookingDocument {

    /** How the document is delivered: email, paper, pdf-invoice. */
    String format();

    boolean requiresSignature();

    /** Full text of the document. */
    String render();
}
