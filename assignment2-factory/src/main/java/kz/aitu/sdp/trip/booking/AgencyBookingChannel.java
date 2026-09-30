package kz.aitu.sdp.trip.booking;

import kz.aitu.sdp.trip.app.Quote;
import kz.aitu.sdp.trip.app.TripPlan;

/** Concrete creator: booked at a desk, 5% service fee, signed paper voucher. */
public final class AgencyBookingChannel extends BookingChannel {

    private static final int SERVICE_FEE_PERCENT = 5;

    @Override
    protected BookingDocument createDocument(TripPlan plan, Customer customer, int price, String reference) {
        return new PaperVoucher(plan, customer, price, reference);
    }

    @Override
    protected int finalPrice(Quote quote, Customer customer) {
        return quote.total() * (100 + SERVICE_FEE_PERCENT) / 100;
    }

    @Override
    protected int paymentDeadlineDays() {
        return 3;
    }

    @Override
    public String name() {
        return "Agency";
    }

    @Override
    protected String referencePrefix() {
        return "AGN";
    }
}
