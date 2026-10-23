package kz.aitu.sdp.route.planner;

import java.util.List;
import java.util.Objects;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.strategy.RouteDecision;
import kz.aitu.sdp.route.strategy.RouteStrategy;

/**
 * Context of the Strategy pattern.
 *
 * <p>The planner knows <em>that</em> a route has to be chosen, never <em>how</em>: it holds a
 * {@link RouteStrategy} and hands the work to it. There is no {@code instanceof} and no switch
 * on strategy names here, so a new strategy needs no change in this class, and
 * {@link #setStrategy} replaces the algorithm on the same object while the program runs.
 */
public class RoutePlanner {

    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(RouteStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy is required");
    }

    public RouteStrategy getStrategy() {
        return strategy;
    }

    /** The strategy gets its own copy of the routes, so it cannot change the caller's list. */
    public RouteDecision plan(List<Route> routes, DeliveryRequest request) {
        Objects.requireNonNull(routes, "candidate routes are required");
        return strategy.selectRoute(List.copyOf(routes), request);
    }
}
