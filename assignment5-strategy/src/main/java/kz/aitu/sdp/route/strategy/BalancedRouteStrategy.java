package kz.aitu.sdp.route.strategy;

import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.Tariff;

/**
 * Weighs time, cost, distance and emission at once.
 *
 * <pre>
 * score = normalizedTime x timeWeight + normalizedCost x costWeight
 *       + normalizedDistance x distanceWeight + normalizedEmission x emissionWeight
 *
 * normalized = (value - min) / (max - min)   over the routes that passed the constraints
 * </pre>
 *
 * Minutes, tenge, kilometres and kilograms cannot be added together, so each criterion is
 * scaled to 0..1 first: 0 is the best candidate on that criterion, 1 the worst. The score is
 * therefore between 0 and 1 too. The weights must be non-negative and add up to 1.0; any other
 * configuration is rejected when the strategy is created, not when it is first used.
 */
public class BalancedRouteStrategy extends ScoredRouteStrategy {

    private static final double TOLERANCE = 1e-9;

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;

    public BalancedRouteStrategy(double timeWeight, double costWeight,
                                 double distanceWeight, double emissionWeight) {
        requireWeight("time", timeWeight);
        requireWeight("cost", costWeight);
        requireWeight("distance", distanceWeight);
        requireWeight("emission", emissionWeight);
        double sum = timeWeight + costWeight + distanceWeight + emissionWeight;
        if (Math.abs(sum - 1.0) > TOLERANCE) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "balanced weights must add up to 1.0, got %.4f", sum));
        }
        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    @Override
    public String getStrategyName() {
        return "Balanced";
    }

    @Override
    protected RouteScorer scorer(List<Route> candidates, DeliveryRequest request) {
        ToDoubleFunction<Route> cost = route -> Tariff.totalCost(route, request);
        Range time = Range.of(candidates, Route::expectedMinutes);
        Range price = Range.of(candidates, cost);
        Range distance = Range.of(candidates, Route::distanceKm);
        Range emission = Range.of(candidates, Route::carbonEmission);

        return route -> {
            double timePart = time.normalize(route.expectedMinutes()) * timeWeight;
            double costPart = price.normalize(cost.applyAsDouble(route)) * costWeight;
            double distancePart = distance.normalize(route.distanceKm()) * distanceWeight;
            double emissionPart = emission.normalize(route.carbonEmission()) * emissionWeight;
            double score = timePart + costPart + distancePart + emissionPart;
            return new RouteScore(score, List.of(
                    line("Time", route.expectedMinutes() + " min", time.normalize(route.expectedMinutes()), timeWeight),
                    line("Cost", Tariff.kzt(cost.applyAsDouble(route)), price.normalize(cost.applyAsDouble(route)), costWeight),
                    line("Distance", route.distanceKm() + " km", distance.normalize(route.distanceKm()), distanceWeight),
                    line("CO2", route.carbonEmission() + " kg", emission.normalize(route.carbonEmission()), emissionWeight),
                    String.format(Locale.ROOT, "Score: %.2f", score)));
        };
    }

    private static String line(String criterion, String value, double normalized, double weight) {
        return String.format(Locale.ROOT, "%s: %s -> %.2f x %.2f = %.3f",
                criterion, value, normalized, weight, normalized * weight);
    }

    private static void requireWeight(String name, double weight) {
        if (!(weight >= 0 && weight <= 1)) {
            throw new IllegalArgumentException(name + " weight must be between 0 and 1, got " + weight);
        }
    }

    private record Range(double min, double max) {

        static Range of(List<Route> routes, ToDoubleFunction<Route> metric) {
            double min = routes.stream().mapToDouble(metric).min().orElse(0);
            double max = routes.stream().mapToDouble(metric).max().orElse(0);
            return new Range(min, max);
        }

        /** 0 for the best value, 1 for the worst; 0 for everyone when all values are equal. */
        double normalize(double value) {
            return max == min ? 0 : (value - min) / (max - min);
        }
    }
}
