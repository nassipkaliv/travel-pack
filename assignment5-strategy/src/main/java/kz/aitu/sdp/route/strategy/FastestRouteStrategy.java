package kz.aitu.sdp.route.strategy;

import java.util.List;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.Tariff;

/**
 * Picks the route that arrives first under the current traffic.
 *
 * <pre>
 * score = estimatedMinutes + traffic penalty
 * traffic penalty = estimatedMinutes x delay factor   LOW 0, MODERATE 0.15, HEAVY 0.40
 * </pre>
 *
 * The shortest free-flow time does not always win: a 25-minute route stuck in heavy traffic
 * takes 35 minutes and loses to a 28-minute route on an empty road. Routes that would miss
 * the deadline never reach this method — the deadline is a hard constraint.
 */
public class FastestRouteStrategy extends ScoredRouteStrategy {

    @Override
    public String getStrategyName() {
        return "Fastest";
    }

    @Override
    protected RouteScorer scorer(List<Route> candidates, DeliveryRequest request) {
        return route -> new RouteScore(route.expectedMinutes(), List.of(
                "Estimated time: " + route.estimatedMinutes() + " min",
                "Traffic penalty: +" + route.trafficDelayMinutes() + " min (" + route.traffic() + " traffic)",
                "Final time: " + route.expectedMinutes() + " min, deadline " + request.maxDeliveryMinutes() + " min",
                "Cost: " + Tariff.kzt(Tariff.totalCost(route, request))));
    }
}
