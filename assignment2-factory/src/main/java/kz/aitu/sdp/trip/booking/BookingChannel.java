package kz.aitu.sdp.trip.booking;

import kz.aitu.sdp.trip.app.Quote;
import kz.aitu.sdp.trip.app.TripPlan;

/**
 * Creator of the Factory Method pattern.
 */
public abstract class BookingChannel {

    private int issuedBookings;

    /** Business logic that works with the product created by the factory method. */
    public final Booking book(TripPlan plan, Quote quote, Customer customer) {
        requireGroupFits(plan, customer);
        int price = finalPrice(quote, customer);
        String reference = nextReference();
        BookingDocument document = createDocument(plan, customer, price, reference);
        return new Booking(reference, plan, customer, price, paymentDeadlineDays(), document);
    }

    /** Uses the created product, so the channel is not just a wrapper around {@code new}. */
    public String confirmationMessage(Booking booking) {
        String signature = booking.document().requiresSignature() ? ", signature required" : "";
        return "%s booking %s for %s: %d KZT, pay within %d days, document sent as %s%s".formatted(
                name(), booking.reference(), booking.customer().name(), booking.pricePaid(),
                booking.paymentDeadlineDays(), booking.document().format(), signature);
    }

    protected abstract BookingDocument createDocument(TripPlan plan, Customer customer, int price, String reference);

    protected abstract int finalPrice(Quote quote, Customer customer);

    protected abstract int paymentDeadlineDays();

    public abstract String name();

    protected abstract String referencePrefix();

    private void requireGroupFits(TripPlan plan, Customer customer) {
        int capacity = plan.excursion().groupSize();
        if (customer.travelers() > capacity) {
            throw new BookingRejectedException(
                    "%s hosts %d travelers, %d requested".formatted(
                            plan.excursion().describe(), capacity, customer.travelers()));
        }
    }

    private String nextReference() {
        issuedBookings++;
        return "%s-%04d".formatted(referencePrefix(), issuedBookings);
    }
}
