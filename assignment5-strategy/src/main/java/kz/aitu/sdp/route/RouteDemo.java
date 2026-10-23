package kz.aitu.sdp.route;

import java.util.List;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.planner.RoutePlanner;
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

    private static void printShort(RouteDecision decision) {
        System.out.printf("    %-9s -> %s%n", decision.strategyName(), decision.route());
    }

    private static void scenario(String title) {
        System.out.println();
        System.out.println(title);
    }
}
