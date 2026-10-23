package kz.aitu.sdp.route.domain;

import java.util.Objects;

/**
 * The business conditions of one order. The same routes give different answers for different
 * requests: the budget, the deadline, the cargo and the emission limit all exclude routes.
 *
 * @param maxEmissionKg upper limit on CO2 for this delivery; {@link Double#POSITIVE_INFINITY}
 *                      when the customer set none
 */
public record DeliveryRequest(
        String id,
        DeliveryPriority priority,
        CargoType cargo,
        double maxBudget,
        int maxDeliveryMinutes,
        double maxEmissionKg,
        boolean ecoPreferred) {

    public DeliveryRequest {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("request id is required");
        }
        Objects.requireNonNull(priority, "delivery priority is required");
        Objects.requireNonNull(cargo, "cargo type is required");
        if (maxBudget <= 0) {
            throw new IllegalArgumentException("budget must be positive: " + maxBudget);
        }
        if (maxDeliveryMinutes <= 0) {
            throw new IllegalArgumentException("deadline must be positive: " + maxDeliveryMinutes);
        }
        if (maxEmissionKg < 0) {
            throw new IllegalArgumentException("emission limit cannot be negative: " + maxEmissionKg);
        }
    }

    /** A request without an emission limit. */
    public DeliveryRequest(String id, DeliveryPriority priority, CargoType cargo,
                           double maxBudget, int maxDeliveryMinutes, boolean ecoPreferred) {
        this(id, priority, cargo, maxBudget, maxDeliveryMinutes, Double.POSITIVE_INFINITY, ecoPreferred);
    }

    public boolean hasEmissionLimit() {
        return Double.isFinite(maxEmissionKg);
    }
}
