package kz.aitu.sdp.trip.booking;

import kz.aitu.sdp.trip.app.Quote;
import kz.aitu.sdp.trip.app.TripPlan;

/**
 * Creator of the Factory Method pattern.
 *
 * <p>{@link #book} is the business logic shared by every channel: it checks that the trip
 * can carry the group, applies the pricing rules of the channel, issues a reference number
 * and hands the customer a document. Which <em>kind</em> of document is produced is decided
 * by the subclass through the factory method {@link #createDocument}.
 *
 * <p>Why a factory method and not a static factory: {@code book} is written once and works
 * for any channel, including channels added later; each channel keeps its own state (the
 * counter behind the reference number) and its own pricing; and tests can plug in their own
 * subclass. A static factory would need a switch over channel names and could not be
 * extended without editing it.
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

    /** Factory method: each channel issues its own kind of document. */
    protected abstract BookingDocument createDocument(TripPlan plan, Customer customer, int price, String reference);

    /** Channel pricing rules: discounts, service fees. */
    protected abstract int finalPrice(Quote quote, Customer customer);

    protected abstract int paymentDeadlineDays();

    public abstract String name();

    protected abstract String referencePrefix();

    /** A group larger than the excursion can host cannot be booked. */
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
