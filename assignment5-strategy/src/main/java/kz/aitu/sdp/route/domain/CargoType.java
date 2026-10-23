package kz.aitu.sdp.route.domain;

/** What is being delivered. Some routes refuse some cargo, and handling costs extra. */
public enum CargoType {
    GENERAL(0),
    FRAGILE(400),
    MEDICAL(600),
    HAZARDOUS(1500);

    private final double handlingFee;

    CargoType(double handlingFee) {
        this.handlingFee = handlingFee;
    }

    public double handlingFee() {
        return handlingFee;
    }
}
