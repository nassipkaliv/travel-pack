package kz.aitu.sdp.trip.booking;

import kz.aitu.sdp.trip.app.TripPlan;

/** Product created by {@link AgencyBookingChannel}. */
class PaperVoucher implements BookingDocument {

    private final TripPlan plan;
    private final Customer customer;
    private final int price;
    private final String reference;

    PaperVoucher(TripPlan plan, Customer customer, int price, String reference) {
        this.plan = plan;
        this.customer = customer;
        this.price = price;
        this.reference = reference;
    }

    @Override
    public String format() {
        return "paper";
    }

    @Override
    public boolean requiresSignature() {
        return true;
    }

    @Override
    public String render() {
        return """
                AGENCY VOUCHER %s
                Traveler: %s (%d persons)
                %s trip to %s, %d nights
                Transport: %s
                Stay:      %s
                Excursion: %s
                Total with service fee: %d KZT
                Signature of the traveler: ______________"""
                .formatted(reference, customer.name(), customer.travelers(), plan.tier(),
                        plan.request().city(), plan.nights(), plan.transport().describe(),
                        plan.stay().describe(), plan.excursion().describe(), price);
    }
}
