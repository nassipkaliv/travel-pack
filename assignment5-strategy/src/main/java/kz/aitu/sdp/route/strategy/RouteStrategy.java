package kz.aitu.sdp.route.strategy;

import java.util.List;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;

/**
 * Strategy of the Strategy pattern: one way of choosing a delivery route.
 *
 * <p>The assignment's sketch returns a bare {@link Route}. This interface returns a
 * {@link RouteDecision} instead, because a route cannot tell why it was chosen or why the
 * others were not — the strategy that made the choice can.
 */
public interface RouteStrategy {

    /**
     * @throws kz.aitu.sdp.route.constraint.NoSuitableRouteException when no route passes the
     *         hard constraints, including when {@code routes} is empty
     */
    RouteDecision selectRoute(List<Route> routes, DeliveryRequest request);

    String getStrategyName();
}
