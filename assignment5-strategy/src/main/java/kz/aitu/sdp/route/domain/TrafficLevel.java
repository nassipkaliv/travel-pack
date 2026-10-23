package kz.aitu.sdp.route.domain;

/**
 * Current traffic on a route. The delay factor is the share of the free-flow time that
 * traffic adds on top of it.
 */
public enum TrafficLevel {
    LOW(0.0),
    MODERATE(0.15),
    HEAVY(0.40);

    private final double delayFactor;

    TrafficLevel(double delayFactor) {
        this.delayFactor = delayFactor;
    }

    public double delayFactor() {
        return delayFactor;
    }

    /** Extra minutes traffic adds to a trip that takes {@code freeFlowMinutes} on an empty road. */
    public int delayMinutes(int freeFlowMinutes) {
        return (int) Math.round(freeFlowMinutes * delayFactor);
    }
}
