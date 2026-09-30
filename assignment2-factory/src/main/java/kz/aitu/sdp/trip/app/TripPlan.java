package kz.aitu.sdp.trip.app;

import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripComponent;
import kz.aitu.sdp.trip.product.TripTier;

/**
 * One consistent trip: three components of the same family plus the request they serve.
 *
 * <p>Consistency comes from the way plans are built — {@link TripPlanner} takes all three
 * components from one factory. The check in the constructor is only a safety net for code
 * that assembles a plan by hand.
 */
public record TripPlan(TripTier tier, Transport transport, Accommodation stay, Excursion excursion,
                       TripRequest request) {

    public TripPlan {
        requireTier(tier, transport);
        requireTier(tier, stay);
        requireTier(tier, excursion);
    }

    private static void requireTier(TripTier tier, TripComponent component) {
        if (component.tier() != tier) {
            throw new IllegalArgumentException(
                    "%s belongs to %s and cannot be part of a %s trip"
                            .formatted(component.describe(), component.tier(), tier));
        }
    }

    public int nights() {
        return request.nights();
    }

    public int travelers() {
        return request.travelers();
    }
}
