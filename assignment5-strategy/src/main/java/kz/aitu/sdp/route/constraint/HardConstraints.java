package kz.aitu.sdp.route.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.Tariff;

/**
 * The set of hard constraints a strategy screens routes with.
 *
 * <p>{@link #standard()} is what every delivery must respect. A strategy with stricter rules
 * adds its own through {@link #with}, without changing this class.
 */
public final class HardConstraints {

    public static final RouteConstraint AVAILABLE = (route, request) ->
            route.available() ? Optional.empty() : Optional.of("route is unavailable");

    public static final RouteConstraint WITHIN_BUDGET = (route, request) -> {
        double total = Tariff.totalCost(route, request);
        return total <= request.maxBudget()
                ? Optional.empty()
                : Optional.of("budget exceeded (" + Tariff.kzt(total) + " > " + Tariff.kzt(request.maxBudget()) + ")");
    };

    public static final RouteConstraint WITHIN_DEADLINE = (route, request) ->
            route.expectedMinutes() <= request.maxDeliveryMinutes()
                    ? Optional.empty()
                    : Optional.of("delivery deadline exceeded (" + route.expectedMinutes()
                            + " min > " + request.maxDeliveryMinutes() + " min)");

    public static final RouteConstraint CARGO_ALLOWED = (route, request) ->
            route.allows(request.cargo())
                    ? Optional.empty()
                    : Optional.of("prohibited for " + request.cargo() + " cargo");

    public static final RouteConstraint WITHIN_EMISSION_LIMIT = (route, request) ->
            route.carbonEmission() <= request.maxEmissionKg()
                    ? Optional.empty()
                    : Optional.of("emission limit exceeded (" + route.carbonEmission()
                            + " kg > " + request.maxEmissionKg() + " kg)");

    private final List<RouteConstraint> constraints;

    private HardConstraints(List<RouteConstraint> constraints) {
        this.constraints = List.copyOf(constraints);
    }

    public static HardConstraints standard() {
        return new HardConstraints(List.of(
                AVAILABLE, WITHIN_BUDGET, WITHIN_DEADLINE, CARGO_ALLOWED, WITHIN_EMISSION_LIMIT));
    }

    /** A copy of this set with one more constraint. */
    public HardConstraints with(RouteConstraint extra) {
        List<RouteConstraint> extended = new ArrayList<>(constraints);
        extended.add(extra);
        return new HardConstraints(extended);
    }

    public Screening screen(List<Route> routes, DeliveryRequest request) {
        List<Route> accepted = new ArrayList<>();
        List<Rejection> rejected = new ArrayList<>();
        for (Route route : routes) {
            List<String> reasons = constraints.stream()
                    .map(constraint -> constraint.violation(route, request))
                    .flatMap(Optional::stream)
                    .toList();
            if (reasons.isEmpty()) {
                accepted.add(route);
            } else {
                rejected.add(new Rejection(route, reasons));
            }
        }
        return new Screening(accepted, rejected);
    }
}
