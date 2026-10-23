package kz.aitu.sdp.route;

import static kz.aitu.sdp.route.Fixtures.request;
import static kz.aitu.sdp.route.Fixtures.route;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.TrafficLevel;
import kz.aitu.sdp.route.planner.RoutePlanner;
import kz.aitu.sdp.route.planner.StrategyComparison;
import kz.aitu.sdp.route.planner.StrategyComparison.Outcome;
import kz.aitu.sdp.route.strategy.FastestRouteStrategy;
import kz.aitu.sdp.route.strategy.PremiumRouteStrategy;
import kz.aitu.sdp.route.strategy.RouteDecision;
import org.junit.jupiter.api.Test;

/** The fifth strategy, added after the others were finished. */
class PremiumStrategyTest {

    @Test
    void premiumPlansForTheWorstCaseWhileFastestTrustsTheForecast() {
        Route cityCentre = route("C", 17, 25, 1400, 4.8, TrafficLevel.HEAVY);   // forecast 35, worst case 45
        Route ringRoad = route("R", 24, 32, 1500, 6.2, TrafficLevel.MODERATE);  // forecast 37, worst case 42
        List<Route> routes = List.of(cityCentre, ringRoad);

        RouteDecision fastest = new FastestRouteStrategy().selectRoute(routes, request(10_000, 44));
        RouteDecision premium = new PremiumRouteStrategy().selectRoute(routes, request(10_000, 44));

        assertAll(
                () -> assertEquals("C", fastest.route().id()),
                () -> assertEquals("R", premium.route().id()),
                () -> assertEquals(42, premium.score().value()),
                () -> assertEquals(List.of("on-time guarantee broken (worst case 45 min > 44 min)"),
                        premium.rejections().get(0).reasons()));
    }

    @Test
    void premiumPlugsIntoTheExistingPlannerSelectorAndComparison() {
        RoutePlanner planner = new RoutePlanner(RoutingConfig.FASTEST);
        planner.setStrategy(RoutingConfig.selector().select(DemoData.PREMIUM_HAZARDOUS));
        List<String> compared = new StrategyComparison(RoutingConfig.strategies())
                .compare(DemoData.routes(), DemoData.COMPARISON).stream()
                .map(Outcome::strategyName)
                .toList();

        assertAll(
                () -> assertSame(RoutingConfig.PREMIUM, planner.getStrategy()),
                () -> assertEquals("R1", planner.plan(DemoData.routes(), DemoData.PREMIUM_HAZARDOUS).route().id()),
                () -> assertSame(RoutingConfig.FASTEST, RoutingConfig.selector().select(DemoData.URGENT_MEDICAL),
                        "an urgent request without the premium budget still goes Fastest"),
                () -> assertTrue(compared.contains("Premium")));
    }
}
