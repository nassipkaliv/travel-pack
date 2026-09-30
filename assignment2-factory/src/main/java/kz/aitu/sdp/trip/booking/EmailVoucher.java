package kz.aitu.sdp.trip.booking;

import kz.aitu.sdp.trip.app.TripPlan;

/** Product created by {@link OnlineBookingChannel}. */
class EmailVoucher implements BookingDocument {

    private final TripPlan plan;
    private final Customer customer;
    private final int price;
    private final String reference;

    EmailVoucher(TripPlan plan, Customer customer, int price, String reference) {
        this.plan = plan;
        this.customer = customer;
        this.price = price;
        this.reference = reference;
    }

    @Override
    public String format() {
        return "email";
    }

    @Override
    public boolean requiresSignature() {
        return false;
    }

    @Override
    public String render() {
        return """
                E-VOUCHER %s
                To: %s
                %s trip to %s, %d nights
                %s / %s / %s
                Paid online: %d KZT
                Show this e-mail at check-in."""
                .formatted(reference, customer.contact(), plan.tier(), plan.request().city(), plan.nights(),
                        plan.transport().describe(), plan.stay().describe(), plan.excursion().describe(), price);
    }
}
