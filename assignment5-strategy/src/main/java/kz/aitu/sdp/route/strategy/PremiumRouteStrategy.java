package kz.aitu.sdp.route.strategy;

import java.util.List;
import java.util.Optional;
import kz.aitu.sdp.route.constraint.HardConstraints;
import kz.aitu.sdp.route.constraint.RouteConstraint;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.Tariff;

/**
 * For customers who pay for a guaranteed time slot: picks the route that is on time even if
 * the traffic turns out twice as bad as forecast.
 *
 * <pre>
 * worst case = estimatedMinutes + 2 x traffic penalty
 * score      = worst case;   worst case > deadline is a hard rejection
 * </pre>
 *
 * Fastest trusts the forecast; Premium plans for the forecast being wrong. A city-centre route
 * in heavy traffic can be the fastest on paper and still lose here to a ring road whose time
 * hardly depends on traffic.
 *
 * <p>Added after the first four strategies. The on-time guarantee is an extra constraint passed
 * to the base class through {@link HardConstraints#with}, so neither the constraints, the base
 * class, the planner nor the selector had to change.
 */
public class PremiumRouteStrategy extends ScoredRouteStrategy {

    static final RouteConstraint ON_TIME_GUARANTEE = (route, request) ->
            worstCaseMinutes(route) <= request.maxDeliveryMinutes()
                    ? Optional.empty()
                    : Optional.of("on-time guarantee broken (worst case " + worstCaseMinutes(route)
                            + " min > " + request.maxDeliveryMinutes() + " min)");

    public PremiumRouteStrategy() {
        super(HardConstraints.standard().with(ON_TIME_GUARANTEE));
    }

    static int worstCaseMinutes(Route route) {
        return route.estimatedMinutes() + 2 * route.trafficDelayMinutes();
    }

    @Override
    public String getStrategyName() {
        return "Premium";
    }

    @Override
    protected RouteScorer scorer(List<Route> candidates, DeliveryRequest request) {
        return route -> new RouteScore(worstCaseMinutes(route), List.of(
                "Estimated time: " + route.estimatedMinutes() + " min",
                "Forecast traffic penalty: +" + route.trafficDelayMinutes() + " min (" + route.traffic() + " traffic)",
                "Worst case, penalty doubled: " + worstCaseMinutes(route) + " min, deadline "
                        + request.maxDeliveryMinutes() + " min",
                "Cost: " + Tariff.kzt(Tariff.totalCost(route, request))));
    }
}
