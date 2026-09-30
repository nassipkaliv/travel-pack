package kz.aitu.sdp.trip.booking;

import kz.aitu.sdp.trip.app.TripPlan;

/** Result of a booking: what was booked, for how much, and with which document. */
public record Booking(String reference, TripPlan plan, Customer customer, int pricePaid,
                      int paymentDeadlineDays, BookingDocument document) {
}
