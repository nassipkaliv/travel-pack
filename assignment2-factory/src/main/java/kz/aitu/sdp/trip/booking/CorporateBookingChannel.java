package kz.aitu.sdp.trip.booking;

import kz.aitu.sdp.trip.app.Quote;
import kz.aitu.sdp.trip.app.TripPlan;

/** Concrete creator: invoiced to a company, group discount, 30 days to pay. */
public final class CorporateBookingChannel extends BookingChannel {

    private static final int GROUP_DISCOUNT_PERCENT = 7;
    private static final int GROUP_SIZE_FOR_DISCOUNT = 5;

    @Override
    protected BookingDocument createDocument(TripPlan plan, Customer customer, int price, String reference) {
        if (customer.companyName().isEmpty()) {
            throw new BookingRejectedException("Corporate booking needs a company to invoice");
        }
        return new CorporateInvoice(plan, customer, price, reference);
    }

    @Override
    protected int finalPrice(Quote quote, Customer customer) {
        boolean groupDiscount = customer.travelers() >= GROUP_SIZE_FOR_DISCOUNT;
        return groupDiscount
                ? quote.total() * (100 - GROUP_DISCOUNT_PERCENT) / 100
                : quote.total();
    }

    @Override
    protected int paymentDeadlineDays() {
        return 30;
    }

    @Override
    public String name() {
        return "Corporate";
    }

    @Override
    protected String referencePrefix() {
        return "COR";
    }
}
