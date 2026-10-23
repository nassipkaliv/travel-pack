package kz.aitu.sdp.route;

import static kz.aitu.sdp.route.Fixtures.request;
import static kz.aitu.sdp.route.Fixtures.route;
import static kz.aitu.sdp.route.Fixtures.unavailable;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import kz.aitu.sdp.route.domain.CargoType;
import kz.aitu.sdp.route.domain.DeliveryPriority;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.TrafficLevel;
import kz.aitu.sdp.route.strategy.BalancedRouteStrategy;
import kz.aitu.sdp.route.strategy.CheapestRouteStrategy;
import kz.aitu.sdp.route.strategy.EcoFriendlyRouteStrategy;
import kz.aitu.sdp.route.strategy.FastestRouteStrategy;
import kz.aitu.sdp.route.strategy.RouteDecision;
import org.junit.jupiter.api.Test;

/** Each concrete strategy ranks by its own formula. */
class StrategyTest {

    private static final double EPS = 1e-9;

    @Test
    void fastestCountsTheTrafficPenaltyNotOnlyTheFreeFlowTime() {
        Route tunnel = route("A", 15, 18, 1000, 2, TrafficLevel.HEAVY);   // 18 + 7 = 25 min
        Route expressway = route("B", 20, 22, 1000, 2, TrafficLevel.LOW); // 22 + 0 = 22 min

        RouteDecision decision = new FastestRouteStrategy().selectRoute(List.of(tunnel, expressway), request(10_000, 60));

        assertAll(
                () -> assertTrue(tunnel.estimatedMinutes() < expressway.estimatedMinutes(),
                        "plain min(estimatedMinutes) would pick the tunnel"),
                () -> assertEquals("B", decision.route().id()),
                () -> assertEquals(22, decision.score().value(), EPS));
    }

    @Test
    void decisionExplainsTheChoiceAndEveryRejection() {
        List<Route> routes = List.of(
                route("A", 15, 18, 1000, 2, TrafficLevel.HEAVY),
                route("B", 20, 22, 1000, 2, TrafficLevel.LOW),
                unavailable("C", 10, 10, 1000, 2));

        String explanation = new FastestRouteStrategy().selectRoute(routes, request(10_000, 60)).explain();

        assertAll(
                () -> assertTrue(explanation.contains("Selected: B")),
                () -> assertTrue(explanation.contains("Traffic penalty: +0 min (LOW traffic)")),
                () -> assertTrue(explanation.contains("Final time: 22 min")),
                () -> assertTrue(explanation.contains("C rejected: route is unavailable")));
    }

    @Test
    void cheapestRanksByTheFullTariffNotByTheBaseCost() {
        DeliveryRequest urgentMedical = new DeliveryRequest("T2", DeliveryPriority.URGENT, CargoType.MEDICAL,
                10_000, 120, false);
        Route lowBaseLongDistance = route("A", 30, 40, 1000, 2, TrafficLevel.LOW);  // 1000 + 1050 + 1500 + 600 = 4150
        Route highBaseShortDistance = route("B", 10, 40, 1400, 2, TrafficLevel.LOW); // 1400 +  350 + 1500 + 600 = 3850

        RouteDecision decision = new CheapestRouteStrategy()
                .selectRoute(List.of(lowBaseLongDistance, highBaseShortDistance), urgentMedical);

        assertAll(
                () -> assertEquals("B", decision.route().id()),
                () -> assertEquals(3850, decision.score().value(), EPS));
    }

    @Test
    void ecoPrefersLowEmissionAndSeparatesEqualEmissionByDistance() {
        List<Route> routes = List.of(
                route("A", 20, 30, 1000, 1.0, TrafficLevel.LOW), // 1.0 + 20 x 0.05 = 2.00
                route("B", 10, 30, 1000, 1.0, TrafficLevel.LOW), // 1.0 + 10 x 0.05 = 1.50
                route("C", 5, 30, 1000, 3.0, TrafficLevel.LOW)); // 3.0 +  5 x 0.05 = 3.25

        RouteDecision decision = new EcoFriendlyRouteStrategy().selectRoute(routes, request(10_000, 60));

        assertAll(
                () -> assertEquals("B", decision.route().id()),
                () -> assertEquals(1.5, decision.score().value(), EPS));
    }

    @Test
    void balancedFollowsItsWeights() {
        Route fast = route("F", 10, 15, 3000, 3, TrafficLevel.LOW);  // total 3650
        Route cheap = route("C", 10, 50, 500, 3, TrafficLevel.LOW);  // total 1150
        List<Route> routes = List.of(fast, cheap);
        DeliveryRequest request = request(10_000, 60);

        RouteDecision timeOnly = new BalancedRouteStrategy(1, 0, 0, 0).selectRoute(routes, request);
        RouteDecision costOnly = new BalancedRouteStrategy(0, 1, 0, 0).selectRoute(routes, request);
        RouteDecision mixed = new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20).selectRoute(routes, request);

        assertAll(
                () -> assertEquals("F", timeOnly.route().id()),
                () -> assertEquals("C", costOnly.route().id()),
                // F: best time (0), worst cost (1 x 0.30) -> 0.30;  C: worst time (1 x 0.40) -> 0.40
                () -> assertEquals("F", mixed.route().id()),
                () -> assertEquals(0.30, mixed.score().value(), EPS));
    }

    @Test
    void balancedScoresARouteBestOnEveryCriterionAsZero() {
        Route dominant = route("D", 10, 20, 1000, 1, TrafficLevel.LOW);
        Route worse = route("W", 20, 40, 2000, 4, TrafficLevel.HEAVY);

        RouteDecision decision = new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20)
                .selectRoute(List.of(worse, dominant), request(10_000, 60));

        assertAll(
                () -> assertEquals("D", decision.route().id()),
                () -> assertEquals(0.0, decision.score().value(), EPS));
    }
}
