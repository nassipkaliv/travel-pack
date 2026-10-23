package kz.aitu.sdp.route;

import static kz.aitu.sdp.route.Fixtures.prohibiting;
import static kz.aitu.sdp.route.Fixtures.request;
import static kz.aitu.sdp.route.Fixtures.route;
import static kz.aitu.sdp.route.Fixtures.unavailable;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kz.aitu.sdp.route.constraint.NoSuitableRouteException;
import kz.aitu.sdp.route.constraint.Rejection;
import kz.aitu.sdp.route.domain.CargoType;
import kz.aitu.sdp.route.domain.DeliveryPriority;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.TrafficLevel;
import kz.aitu.sdp.route.planner.RoutePlanner;
import kz.aitu.sdp.route.strategy.CheapestRouteStrategy;
import kz.aitu.sdp.route.strategy.EcoFriendlyRouteStrategy;
import kz.aitu.sdp.route.strategy.FastestRouteStrategy;
import kz.aitu.sdp.route.strategy.RouteDecision;
import org.junit.jupiter.api.Test;

/** A route that breaks a hard constraint never wins, however good its score. */
class ConstraintTest {

    @Test
    void budgetRejectsTheFastestRoute() {
        Route fastButExpensive = route("A", 10, 15, 5000, 2, TrafficLevel.LOW); // 5000 + 350 + 300 = 5650
        Route slowerAndCheap = route("B", 10, 30, 1000, 2, TrafficLevel.LOW);   // 1000 + 350 + 300 = 1650

        RouteDecision decision = new FastestRouteStrategy()
                .selectRoute(List.of(fastButExpensive, slowerAndCheap), request(4_000, 60));

        assertAll(
                () -> assertEquals("B", decision.route().id()),
                () -> assertEquals("A", decision.rejections().get(0).route().id()),
                () -> assertTrue(decision.rejections().get(0).reasons().get(0).startsWith("budget exceeded")));
    }

    @Test
    void deadlineIsCheckedAgainstTheTimeWithTraffic() {
        Route cheapButJammed = route("A", 10, 30, 100, 2, TrafficLevel.HEAVY); // 30 + 12 = 42 min
        Route pricierOnTime = route("B", 10, 30, 1000, 2, TrafficLevel.LOW);   // 30 min

        RouteDecision decision = new CheapestRouteStrategy()
                .selectRoute(List.of(cheapButJammed, pricierOnTime), request(10_000, 40));

        assertAll(
                () -> assertEquals("B", decision.route().id()),
                () -> assertEquals(List.of("delivery deadline exceeded (42 min > 40 min)"),
                        decision.rejections().get(0).reasons()));
    }

    @Test
    void unavailableProhibitedAndHighEmissionRoutesAreFilteredOut() {
        DeliveryRequest hazardousEco = new DeliveryRequest("T3", DeliveryPriority.ECO, CargoType.HAZARDOUS,
                10_000, 60, 3.0, true);
        List<Route> routes = List.of(
                unavailable("U", 10, 20, 1000, 0.0),
                prohibiting(CargoType.HAZARDOUS, "H", 10, 20, 1000, 0.1),
                route("E", 10, 20, 1000, 5.0, TrafficLevel.LOW),
                route("OK", 10, 20, 1000, 2.0, TrafficLevel.LOW));

        RouteDecision decision = new EcoFriendlyRouteStrategy().selectRoute(routes, hazardousEco);

        Set<String> rejected = decision.rejections().stream()
                .map(rejection -> rejection.route().id())
                .collect(Collectors.toSet());
        assertAll(
                () -> assertEquals("OK", decision.route().id(), "the three greener routes are all illegal"),
                () -> assertEquals(Set.of("U", "H", "E"), rejected));
    }

    @Test
    void costExactlyAtTheBudgetAndTimeExactlyAtTheDeadlineAreAccepted() {
        Route onTheLimit = route("L", 10, 40, 1350, 2, TrafficLevel.MODERATE); // 1350 + 350 + 300 = 2000; 40 + 6 = 46 min
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        RouteDecision accepted = planner.plan(List.of(onTheLimit), request(2_000, 46));

        assertAll(
                () -> assertEquals("L", accepted.route().id()),
                () -> assertTrue(accepted.rejections().isEmpty()),
                () -> assertThrows(NoSuitableRouteException.class,
                        () -> planner.plan(List.of(onTheLimit), request(1_999.99, 46))),
                () -> assertThrows(NoSuitableRouteException.class,
                        () -> planner.plan(List.of(onTheLimit), request(2_000, 45))));
    }

    @Test
    void noValidRouteThrowsWithTheReasonForEveryRoute() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        NoSuitableRouteException failure = assertThrows(NoSuitableRouteException.class,
                () -> planner.plan(DemoData.routes(), DemoData.IMPOSSIBLE));

        assertAll(
                () -> assertEquals(DemoData.routes().size(), failure.rejections().size()),
                () -> assertFalse(failure.rejections().stream().map(Rejection::reasons).anyMatch(List::isEmpty)),
                () -> assertEquals(DemoData.IMPOSSIBLE, failure.request()),
                () -> assertTrue(failure.getMessage().contains("request #106")));
    }
}
