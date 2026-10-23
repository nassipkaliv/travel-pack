package kz.aitu.sdp.route;

import java.util.List;
import kz.aitu.sdp.route.domain.DeliveryPriority;
import kz.aitu.sdp.route.planner.StrategySelector;
import kz.aitu.sdp.route.strategy.BalancedRouteStrategy;
import kz.aitu.sdp.route.strategy.CheapestRouteStrategy;
import kz.aitu.sdp.route.strategy.EcoFriendlyRouteStrategy;
import kz.aitu.sdp.route.strategy.FastestRouteStrategy;
import kz.aitu.sdp.route.strategy.RouteStrategy;

/**
 * The one place that knows the concrete strategies and the business rules that pick them.
 * Planner and selector only see {@link RouteStrategy}.
 */
public final class RoutingConfig {

    public static final RouteStrategy FASTEST = new FastestRouteStrategy();
    public static final RouteStrategy CHEAPEST = new CheapestRouteStrategy();
    public static final RouteStrategy ECO = new EcoFriendlyRouteStrategy();
    public static final RouteStrategy BALANCED = new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20);

    private RoutingConfig() {
    }

    /** Every strategy the comparison mode runs. */
    public static List<RouteStrategy> strategies() {
        return List.of(FASTEST, CHEAPEST, ECO, BALANCED);
    }

    /** Rules are checked in order: an URGENT request that prefers eco still goes Fastest. */
    public static StrategySelector selector() {
        return new StrategySelector(BALANCED)
                .when("URGENT priority", request -> request.priority() == DeliveryPriority.URGENT, FASTEST)
                .when("ECONOMY priority", request -> request.priority() == DeliveryPriority.ECONOMY, CHEAPEST)
                .when("ECO priority or eco preferred",
                        request -> request.priority() == DeliveryPriority.ECO || request.ecoPreferred(), ECO);
    }
}
