package kz.aitu.sdp.route.strategy;

import java.util.List;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.Tariff;
import kz.aitu.sdp.route.domain.Tariff.CostBreakdown;

/**
 * Picks the route with the lowest total price for the customer.
 *
 * <pre>
 * score = base delivery cost + distanceKm x 35 + priority surcharge + cargo handling fee
 * </pre>
 *
 * The formula itself is {@link Tariff}, because the budget constraint must use the same
 * price. This strategy owns the decision to rank by that price and nothing else. The surcharge
 * and the fee are the same for every route of one request, so they do not change the order —
 * they are shown because they change whether the route fits the budget.
 */
public class CheapestRouteStrategy extends ScoredRouteStrategy {

    @Override
    public String getStrategyName() {
        return "Cheapest";
    }

    @Override
    protected RouteScorer scorer(List<Route> candidates, DeliveryRequest request) {
        return route -> {
            CostBreakdown price = Tariff.price(route, request);
            return new RouteScore(price.total(), List.of(
                    "Base delivery cost: " + Tariff.kzt(price.base()),
                    "Distance cost: " + route.distanceKm() + " km x " + (int) Tariff.RATE_PER_KM
                            + " = " + Tariff.kzt(price.distance()),
                    "Priority surcharge (" + request.priority() + "): " + Tariff.kzt(price.prioritySurcharge()),
                    "Cargo handling fee (" + request.cargo() + "): " + Tariff.kzt(price.fees()),
                    "Total cost: " + Tariff.kzt(price.total()) + ", budget " + Tariff.kzt(request.maxBudget()),
                    "Time: " + route.expectedMinutes() + " min"));
        };
    }
}
