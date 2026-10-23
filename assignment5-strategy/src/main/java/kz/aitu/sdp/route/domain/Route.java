package kz.aitu.sdp.route.domain;

import java.util.Objects;
import java.util.Set;

/**
 * One candidate way to deliver an order: a path through the city and the vehicle that drives it.
 *
 * <p>{@code estimatedMinutes} is the free-flow time; {@link #expectedMinutes()} adds the delay
 * of the current traffic. {@code cost} is the carrier's base price for the route — the full
 * price the customer pays is computed by {@link Tariff}.
 */
public record Route(
        String id,
        String name,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double cost,
        double carbonEmission,
        TrafficLevel traffic,
        boolean available,
        Set<CargoType> prohibitedCargo) {

    public Route {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("route id is required");
        }
        Objects.requireNonNull(name, "route name is required");
        Objects.requireNonNull(origin, "origin is required");
        Objects.requireNonNull(destination, "destination is required");
        Objects.requireNonNull(traffic, "traffic level is required");
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("distance must be positive: " + distanceKm);
        }
        if (estimatedMinutes <= 0) {
            throw new IllegalArgumentException("estimated time must be positive: " + estimatedMinutes);
        }
        if (cost < 0) {
            throw new IllegalArgumentException("cost cannot be negative: " + cost);
        }
        if (carbonEmission < 0) {
            throw new IllegalArgumentException("carbon emission cannot be negative: " + carbonEmission);
        }
        prohibitedCargo = Set.copyOf(prohibitedCargo);
    }

    public int trafficDelayMinutes() {
        return traffic.delayMinutes(estimatedMinutes);
    }

    /** Free-flow time plus the traffic delay — what the deadline is checked against. */
    public int expectedMinutes() {
        return estimatedMinutes + trafficDelayMinutes();
    }

    public boolean allows(CargoType cargo) {
        return !prohibitedCargo.contains(cargo);
    }

    @Override
    public String toString() {
        return id + " (" + name + ")";
    }
}
