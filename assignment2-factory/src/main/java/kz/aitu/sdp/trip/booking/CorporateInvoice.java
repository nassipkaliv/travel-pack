package kz.aitu.sdp.trip.booking;

import kz.aitu.sdp.trip.app.TripPlan;

/** Product created by {@link CorporateBookingChannel}. */
class CorporateInvoice implements BookingDocument {

    private static final int VAT_PERCENT = 12;

    private final TripPlan plan;
    private final Customer customer;
    private final int price;
    private final String reference;

    CorporateInvoice(TripPlan plan, Customer customer, int price, String reference) {
        this.plan = plan;
        this.customer = customer;
        this.price = price;
        this.reference = reference;
    }

    @Override
    public String format() {
        return "pdf-invoice";
    }

    @Override
    public boolean requiresSignature() {
        return false;
    }

    @Override
    public String render() {
        int vat = price * VAT_PERCENT / (100 + VAT_PERCENT);
        return """
                INVOICE %s
                Bill to: %s
                Employees: %s (%d persons)
                Service: %s trip to %s, %d nights (%s, %s, %s)
                Amount: %d KZT, VAT %d%% included: %d KZT"""
                .formatted(reference, customer.companyName().orElseThrow(), customer.name(),
                        customer.travelers(), plan.tier(), plan.request().city(), plan.nights(),
                        plan.transport().describe(), plan.stay().describe(), plan.excursion().describe(),
                        price, VAT_PERCENT, vat);
    }
}
