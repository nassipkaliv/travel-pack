package kz.aitu.sdp.route;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import kz.aitu.sdp.route.domain.CargoType;
import kz.aitu.sdp.route.domain.DeliveryPriority;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.Tariff;
import kz.aitu.sdp.route.planner.RoutePlanner;
import kz.aitu.sdp.route.planner.StrategyComparison;
import kz.aitu.sdp.route.planner.StrategyComparison.Chosen;
import kz.aitu.sdp.route.planner.StrategyComparison.NoRoute;
import kz.aitu.sdp.route.planner.StrategyComparison.Outcome;
import kz.aitu.sdp.route.planner.StrategySelector;
import kz.aitu.sdp.route.strategy.RouteDecision;
import kz.aitu.sdp.route.strategy.RouteStrategy;
import org.junit.jupiter.api.Test;

/** The context, the selector and the comparison mode, on the demo network. */
class PlannerTest {

    private final List<Route> routes = DemoData.routes();

    @Test
    void samePlannerChoosesDifferentRoutesAfterEachSwitch() {
        RoutePlanner planner = new RoutePlanner(RoutingConfig.FASTEST);
        String fastest = planner.plan(routes, DemoData.COMPARISON).route().id();

        planner.setStrategy(RoutingConfig.CHEAPEST);
        String cheapest = planner.plan(routes, DemoData.COMPARISON).route().id();

        planner.setStrategy(RoutingConfig.ECO);
        String eco = planner.plan(routes, DemoData.COMPARISON).route().id();

        planner.setStrategy(RoutingConfig.BALANCED);
        String balanced = planner.plan(routes, DemoData.COMPARISON).route().id();

        assertAll(
                () -> assertEquals(List.of("R2", "R5", "R6", "R7"), List.of(fastest, cheapest, eco, balanced)),
                () -> assertSame(RoutingConfig.BALANCED, planner.getStrategy()));
    }

    @Test
    void selectorMapsTheRequestToTheMatchingStrategy() {
        StrategySelector selector = RoutingConfig.selector();

        assertAll(
                () -> assertSame(RoutingConfig.FASTEST, selector.select(DemoData.URGENT_MEDICAL)),
                () -> assertSame(RoutingConfig.CHEAPEST, selector.select(DemoData.ECONOMY_PARCEL)),
                () -> assertSame(RoutingConfig.ECO, selector.select(DemoData.ECO_PARCEL)),
                () -> assertSame(RoutingConfig.BALANCED, selector.select(DemoData.STANDARD_FRAGILE)),
                () -> assertSame(RoutingConfig.ECO, selector.select(standard(CargoType.GENERAL, true)),
                        "a STANDARD request that prefers eco goes to the eco strategy"));
    }

    @Test
    void selectorChecksRulesInOrderAndAcceptsNewRulesWithoutChanges() {
        DeliveryRequest urgentButEco = new DeliveryRequest("T4", DeliveryPriority.URGENT, CargoType.GENERAL,
                10_000, 40, true);
        StrategySelector selector = RoutingConfig.selector()
                .when("hazardous cargo", request -> request.cargo() == CargoType.HAZARDOUS, RoutingConfig.CHEAPEST);

        assertAll(
                () -> assertSame(RoutingConfig.FASTEST, selector.select(urgentButEco), "URGENT rule comes first"),
                () -> assertSame(RoutingConfig.CHEAPEST, selector.select(standard(CargoType.HAZARDOUS, false))),
                () -> assertEquals("hazardous cargo", selector.choose(standard(CargoType.HAZARDOUS, false)).rule()));
    }

    @Test
    void differentRequestsLeadToDifferentRoutes() {
        StrategySelector selector = RoutingConfig.selector();
        RoutePlanner planner = new RoutePlanner(RoutingConfig.BALANCED);

        List<String> chosen = List.of(DemoData.URGENT_MEDICAL, DemoData.ECONOMY_PARCEL,
                        DemoData.ECO_PARCEL, DemoData.STANDARD_FRAGILE).stream()
                .map(request -> {
                    planner.setStrategy(selector.select(request));
                    return planner.plan(routes, request).route().id();
                })
                .toList();

        assertEquals(List.of("R2", "R5", "R6", "R7"), chosen);
    }

    @Test
    void comparisonRunsEveryStrategyOnTheSameRequest() {
        List<Outcome> outcomes = new StrategyComparison(RoutingConfig.strategies()).compare(routes, DemoData.COMPARISON);
        RouteDecision fastest = ((Chosen) outcomes.get(0)).decision();
        RouteDecision cheapest = ((Chosen) outcomes.get(1)).decision();

        assertAll(
                () -> assertEquals(RoutingConfig.strategies().stream().map(RouteStrategy::getStrategyName).toList(),
                        outcomes.stream().map(Outcome::strategyName).toList()),
                () -> assertEquals(List.of("Fastest", "Cheapest"),
                        outcomes.stream().limit(2).map(Outcome::strategyName).toList()),
                () -> assertNotEquals(fastest.route(), cheapest.route()),
                () -> assertTrue(fastest.route().expectedMinutes() < cheapest.route().expectedMinutes()),
                () -> assertTrue(Tariff.totalCost(cheapest.route(), DemoData.COMPARISON)
                        < Tariff.totalCost(fastest.route(), DemoData.COMPARISON)));
    }

    @Test
    void comparisonReportsStrategiesThatFindNothing() {
        List<Outcome> outcomes = new StrategyComparison(RoutingConfig.strategies()).compare(routes, DemoData.IMPOSSIBLE);

        assertAll(
                () -> assertEquals(RoutingConfig.strategies().size(), outcomes.size()),
                () -> outcomes.forEach(outcome -> assertInstanceOf(NoRoute.class, outcome)));
    }

    private static DeliveryRequest standard(CargoType cargo, boolean ecoPreferred) {
        return new DeliveryRequest("T5", DeliveryPriority.STANDARD, cargo, 10_000, 60, ecoPreferred);
    }
}
