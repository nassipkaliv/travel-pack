package kz.aitu.sdp.route.planner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import kz.aitu.sdp.route.constraint.NoSuitableRouteException;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.Tariff;
import kz.aitu.sdp.route.strategy.RouteDecision;
import kz.aitu.sdp.route.strategy.RouteStrategy;

/**
 * Runs every strategy on the same request with one planner and lines the answers up.
 *
 * <p>A strategy that finds nothing does not stop the comparison: its row says so and the
 * remaining strategies still run.
 */
public final class StrategyComparison {

    private final List<RouteStrategy> strategies;

    public StrategyComparison(List<RouteStrategy> strategies) {
        if (strategies.isEmpty()) {
            throw new IllegalArgumentException("at least one strategy is required to compare");
        }
        this.strategies = List.copyOf(strategies);
    }

    public List<Outcome> compare(List<Route> routes, DeliveryRequest request) {
        RoutePlanner planner = new RoutePlanner(strategies.get(0));
        List<Outcome> outcomes = new ArrayList<>();
        for (RouteStrategy strategy : strategies) {
            planner.setStrategy(strategy);
            try {
                outcomes.add(new Chosen(planner.plan(routes, request)));
            } catch (NoSuitableRouteException noRoute) {
                outcomes.add(new NoRoute(strategy.getStrategyName(), noRoute));
            }
        }
        return outcomes;
    }

    public String report(List<Route> routes, DeliveryRequest request) {
        StringBuilder text = new StringBuilder();
        text.append(String.format(Locale.ROOT, "DELIVERY REQUEST #%s (%s, %s cargo, budget %s, deadline %d min)%n%n",
                request.id(), request.priority(), request.cargo(),
                Tariff.kzt(request.maxBudget()), request.maxDeliveryMinutes()));
        text.append(String.format(Locale.ROOT, "%-10s %-34s %7s %11s %7s %8s%n",
                "Strategy", "Route", "Time", "Cost", "CO2", "Score"));
        for (Outcome outcome : compare(routes, request)) {
            if (outcome instanceof Chosen chosen) {
                RouteDecision decision = chosen.decision();
                Route route = decision.route();
                text.append(String.format(Locale.ROOT, "%-10s %-34s %3d min %11s %4.1f kg %8.2f%n",
                        decision.strategyName(), route, route.expectedMinutes(),
                        Tariff.kzt(Tariff.totalCost(route, request)), route.carbonEmission(),
                        decision.score().value()));
            } else {
                text.append(String.format(Locale.ROOT, "%-10s no suitable route%n", outcome.strategyName()));
            }
        }
        return text.toString().stripTrailing();
    }

    /** What one strategy answered: a decision or the reason there is none. */
    public sealed interface Outcome permits Chosen, NoRoute {
        String strategyName();
    }

    public record Chosen(RouteDecision decision) implements Outcome {

        @Override
        public String strategyName() {
            return decision.strategyName();
        }
    }

    public record NoRoute(String strategyName, NoSuitableRouteException reason) implements Outcome {
    }
}
