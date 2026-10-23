package kz.aitu.sdp.route;

import java.util.List;
import java.util.Set;
import kz.aitu.sdp.route.constraint.NoSuitableRouteException;
import kz.aitu.sdp.route.domain.CargoType;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.Tariff;
import kz.aitu.sdp.route.domain.TrafficLevel;
import kz.aitu.sdp.route.planner.RoutePlanner;
import kz.aitu.sdp.route.planner.StrategyComparison;
import kz.aitu.sdp.route.planner.StrategySelector;
import kz.aitu.sdp.route.planner.StrategySelector.Selection;
import kz.aitu.sdp.route.strategy.BalancedRouteStrategy;
import kz.aitu.sdp.route.strategy.CheapestRouteStrategy;
import kz.aitu.sdp.route.strategy.EcoFriendlyRouteStrategy;
import kz.aitu.sdp.route.strategy.FastestRouteStrategy;
import kz.aitu.sdp.route.strategy.RouteDecision;

/** Client: builds the planner, feeds it requests and prints what it decided. */
public final class RouteDemo {

    private final List<Route> routes = DemoData.routes();

    public static void main(String[] args) {
        RouteDemo demo = new RouteDemo();
        demo.switchStrategiesAtRuntime();
        demo.selectStrategyAutomatically();
        demo.compareAllStrategies();
        demo.failureScenarios();
    }

    /** One planner object, four algorithms, three switches. */
    private void switchStrategiesAtRuntime() {
        scenario("1. Runtime strategy switching on one RoutePlanner, request #" + DemoData.COMPARISON.id());
        DeliveryRequest request = DemoData.COMPARISON;
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        printShort(planner.plan(routes, request));

        planner.setStrategy(new CheapestRouteStrategy());
        printShort(planner.plan(routes, request));

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        printShort(planner.plan(routes, request));

        planner.setStrategy(new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20));
        printShort(planner.plan(routes, request));
        System.out.println("    the same planner object served all four strategies");
    }

    /** The selector picks the strategy, the planner runs it, the decision explains itself. */
    private void selectStrategyAutomatically() {
        scenario("2. Automatic strategy selection");
        StrategySelector selector = RoutingConfig.selector();
        RoutePlanner planner = new RoutePlanner(RoutingConfig.BALANCED);
        for (DeliveryRequest request : List.of(DemoData.URGENT_MEDICAL, DemoData.ECONOMY_PARCEL,
                DemoData.ECO_PARCEL, DemoData.STANDARD_FRAGILE)) {
            Selection selection = selector.choose(request);
            planner.setStrategy(selection.strategy());
            RouteDecision decision = planner.plan(routes, request);

            System.out.printf("%n  Request #%s: %s, %s cargo, budget %s, deadline %d min%n",
                    request.id(), request.priority(), request.cargo(),
                    Tariff.kzt(request.maxBudget()), request.maxDeliveryMinutes());
            System.out.printf("  rule \"%s\" -> %s strategy%n", selection.rule(), selection.strategy().getStrategyName());
            System.out.println(decision.explain().indent(4).stripTrailing());
        }
    }

    private void compareAllStrategies() {
        scenario("3. Strategy comparison mode");
        StrategyComparison comparison = new StrategyComparison(RoutingConfig.strategies());
        System.out.println(comparison.report(routes, DemoData.COMPARISON).indent(4).stripTrailing());
    }

    private void failureScenarios() {
        scenario("4. Failure scenarios");
        RoutePlanner planner = new RoutePlanner(RoutingConfig.FASTEST);

        attempt("no route satisfies the constraints", () -> planner.plan(routes, DemoData.IMPOSSIBLE));
        attempt("empty route list", () -> planner.plan(List.of(), DemoData.COMPARISON));
        attempt("balanced weights add up to 1.2", () -> new BalancedRouteStrategy(0.50, 0.50, 0.20, 0.00));
        attempt("negative distance", () -> new Route("R9", "Broken record", DemoData.HUB, DemoData.EXPO,
                -3, 10, 500, 1.0, TrafficLevel.LOW, true, Set.of()));
        attempt("missing delivery priority", () -> new DeliveryRequest("107", null, CargoType.GENERAL, 5_000, 60, false));
    }

    private static void attempt(String title, Runnable action) {
        System.out.println("  " + title + ":");
        try {
            action.run();
            System.out.println("    unexpectedly succeeded");
        } catch (NoSuitableRouteException | IllegalArgumentException | NullPointerException failure) {
            System.out.printf("    %s: %s%n", failure.getClass().getSimpleName(),
                    failure.getMessage().indent(4).strip());
        }
    }

    private static void printShort(RouteDecision decision) {
        System.out.printf("    %-9s -> %s%n", decision.strategyName(), decision.route());
    }

    private static void scenario(String title) {
        System.out.println();
        System.out.println(title);
    }
}
