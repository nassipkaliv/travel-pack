package kz.aitu.sdp.trip.booking;

import kz.aitu.sdp.trip.app.Quote;
import kz.aitu.sdp.trip.app.TripPlan;

/** Concrete creator: self-service booking, paid immediately, 3% cheaper. */
public final class OnlineBookingChannel extends BookingChannel {

    private static final int DISCOUNT_PERCENT = 3;

    @Override
    protected BookingDocument createDocument(TripPlan plan, Customer customer, int price, String reference) {
        return new EmailVoucher(plan, customer, price, reference);
    }

    @Override
    protected int finalPrice(Quote quote, Customer customer) {
        return quote.total() * (100 - DISCOUNT_PERCENT) / 100;
    }

    @Override
    protected int paymentDeadlineDays() {
        return 0;
    }

    @Override
    public String name() {
        return "Online";
    }

    @Override
    protected String referencePrefix() {
        return "ONL";
    }
}
