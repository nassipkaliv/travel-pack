package kz.aitu.sdp.route.strategy;

import java.util.List;
import java.util.Locale;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;

/**
 * Picks the route with the smallest environmental impact.
 *
 * <pre>
 * ecoScore = carbonEmission x emissionWeight + distanceKm x distanceWeight
 * </pre>
 *
 * CO2 dominates; distance stands for what CO2 does not count — tyre wear, noise and the road
 * space the vehicle takes — and separates two routes that emit about the same.
 *
 * <p>The score lives here and not in {@link Route} because it is not a fact about the route,
 * it is one opinion of what "good" means. Another strategy weighs the same emission
 * differently ({@link BalancedRouteStrategy}) or ignores it. Putting the formula in the route
 * would force that opinion on every strategy.
 */
public class EcoFriendlyRouteStrategy extends ScoredRouteStrategy {

    public static final double DEFAULT_EMISSION_WEIGHT = 1.0;
    public static final double DEFAULT_DISTANCE_WEIGHT = 0.05;

    private final double emissionWeight;
    private final double distanceWeight;

    public EcoFriendlyRouteStrategy() {
        this(DEFAULT_EMISSION_WEIGHT, DEFAULT_DISTANCE_WEIGHT);
    }

    public EcoFriendlyRouteStrategy(double emissionWeight, double distanceWeight) {
        if (!(emissionWeight >= 0) || !(distanceWeight >= 0)
                || Double.isInfinite(emissionWeight) || Double.isInfinite(distanceWeight)) {
            throw new IllegalArgumentException("eco weights must be finite and non-negative");
        }
        if (emissionWeight == 0 && distanceWeight == 0) {
            throw new IllegalArgumentException("at least one eco weight must be positive");
        }
        this.emissionWeight = emissionWeight;
        this.distanceWeight = distanceWeight;
    }

    @Override
    public String getStrategyName() {
        return "Eco";
    }

    @Override
    protected RouteScorer scorer(List<Route> candidates, DeliveryRequest request) {
        return route -> {
            double emissionPart = route.carbonEmission() * emissionWeight;
            double distancePart = route.distanceKm() * distanceWeight;
            double ecoScore = emissionPart + distancePart;
            return new RouteScore(ecoScore, List.of(
                    String.format(Locale.ROOT, "CO2: %.1f kg x %.2f = %.2f",
                            route.carbonEmission(), emissionWeight, emissionPart),
                    String.format(Locale.ROOT, "Distance: %.1f km x %.2f = %.2f",
                            route.distanceKm(), distanceWeight, distancePart),
                    String.format(Locale.ROOT, "Eco score: %.2f", ecoScore)));
        };
    }
}
