package kz.aitu.sdp.route.domain;

import java.util.Locale;

/**
 * The price the customer pays for a route, in tenge:
 *
 * <pre>
 * total = base delivery cost            route.cost()
 *       + distance cost                 distanceKm x 35
 *       + priority surcharge            URGENT 1500, STANDARD 300, ECONOMY and ECO 0
 *       + cargo handling fee            FRAGILE 400, MEDICAL 600, HAZARDOUS 1500
 * </pre>
 *
 * <p>The price is a business rule, not a route-selection algorithm: the budget constraint and
 * every strategy that shows a cost must see the same number, so it lives in the domain and
 * the strategies only decide how much weight to give it.
 */
public final class Tariff {

    public static final double RATE_PER_KM = 35;

    private Tariff() {
    }

    public static CostBreakdown price(Route route, DeliveryRequest request) {
        return new CostBreakdown(
                route.cost(),
                route.distanceKm() * RATE_PER_KM,
                request.priority().surcharge(),
                request.cargo().handlingFee());
    }

    public static double totalCost(Route route, DeliveryRequest request) {
        return price(route, request).total();
    }

    public static String kzt(double amount) {
        return String.format(Locale.ROOT, "%,.0f KZT", amount);
    }

    public record CostBreakdown(double base, double distance, double prioritySurcharge, double fees) {

        public double total() {
            return base + distance + prioritySurcharge + fees;
        }
    }
}
