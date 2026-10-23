package kz.aitu.sdp.route;

import static kz.aitu.sdp.route.Fixtures.request;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import kz.aitu.sdp.route.constraint.NoSuitableRouteException;
import kz.aitu.sdp.route.domain.CargoType;
import kz.aitu.sdp.route.domain.DeliveryPriority;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.TrafficLevel;
import kz.aitu.sdp.route.planner.RoutePlanner;
import kz.aitu.sdp.route.strategy.BalancedRouteStrategy;
import org.junit.jupiter.api.Test;

/** Invalid input is refused where it enters, not deep inside an algorithm. */
class FailureTest {

    @Test
    void invalidBalancedWeightsAreRejectedAtConstruction() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(0.5, 0.5, 0.2, 0.0)),
                () -> assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.1)),
                () -> assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(-0.1, 0.5, 0.3, 0.3)),
                () -> assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(Double.NaN, 0.5, 0.3, 0.2)),
                () -> assertDoesNotThrow(() -> new BalancedRouteStrategy(1.0, 0.0, 0.0, 0.0)));
    }

    @Test
    void emptyRouteListMeansNoSuitableRoute() {
        RoutePlanner planner = new RoutePlanner(RoutingConfig.CHEAPEST);

        NoSuitableRouteException failure = assertThrows(NoSuitableRouteException.class,
                () -> planner.plan(List.of(), request(5_000, 60)));

        assertAll(
                () -> assertTrue(failure.rejections().isEmpty()),
                () -> assertTrue(failure.getMessage().contains("no candidate routes were supplied")));
    }

    @Test
    void routeWithNegativeCostOrDistanceCannotBeCreated() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Route("X", "x", DemoData.HUB, DemoData.EXPO,
                        -3, 10, 500, 1, TrafficLevel.LOW, true, Set.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> new Route("X", "x", DemoData.HUB, DemoData.EXPO,
                        3, 10, -500, 1, TrafficLevel.LOW, true, Set.of())));
    }

    @Test
    void requestWithMissingDataCannotBeCreated() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new DeliveryRequest("1", null, CargoType.GENERAL, 5_000, 60, false)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new DeliveryRequest(" ", DeliveryPriority.STANDARD, CargoType.GENERAL, 5_000, 60, false)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new DeliveryRequest("1", DeliveryPriority.STANDARD, CargoType.GENERAL, 0, 60, false)));
    }
}
