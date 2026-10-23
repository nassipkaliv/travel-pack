package kz.aitu.sdp.route.domain;

/** Service level the customer ordered. Faster service is charged a surcharge. */
public enum DeliveryPriority {
    URGENT(1500),
    STANDARD(300),
    ECONOMY(0),
    ECO(0);

    private final double surcharge;

    DeliveryPriority(double surcharge) {
        this.surcharge = surcharge;
    }

    public double surcharge() {
        return surcharge;
    }
}
