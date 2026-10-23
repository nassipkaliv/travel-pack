package kz.aitu.sdp.route.strategy;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import kz.aitu.sdp.route.constraint.HardConstraints;
import kz.aitu.sdp.route.constraint.NoSuitableRouteException;
import kz.aitu.sdp.route.constraint.Screening;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.Tariff;

/**
 * The steps every strategy shares, in a fixed order: screen the routes with the hard
 * constraints, score what is left, take the lowest score.
 *
 * <p>Only the scoring differs between strategies, so that is the one method a subclass
 * writes. Because screening happens here, before {@link #scorer} is ever called, a route that
 * breaks a constraint cannot win by having a good score. Ties go to the cheaper route, then
 * to the lower id, so the same input always gives the same answer.
 */
public abstract class ScoredRouteStrategy implements RouteStrategy {

    private final HardConstraints constraints;

    protected ScoredRouteStrategy() {
        this(HardConstraints.standard());
    }

    protected ScoredRouteStrategy(HardConstraints constraints) {
        this.constraints = Objects.requireNonNull(constraints, "constraints are required");
    }

    @Override
    public final RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        Objects.requireNonNull(routes, "candidate routes are required");
        Objects.requireNonNull(request, "delivery request is required");

        Screening screening = constraints.screen(routes, request);
        if (screening.accepted().isEmpty()) {
            throw new NoSuitableRouteException(getStrategyName(), request, screening.rejected());
        }

        RouteScorer scorer = scorer(screening.accepted(), request);
        record Candidate(Route route, RouteScore score) {
        }
        Candidate best = screening.accepted().stream()
                .map(route -> new Candidate(route, scorer.score(route)))
                .min(Comparator.comparingDouble((Candidate candidate) -> candidate.score().value())
                        .thenComparingDouble(candidate -> Tariff.totalCost(candidate.route(), request))
                        .thenComparing(candidate -> candidate.route().id()))
                .orElseThrow();
        return new RouteDecision(getStrategyName(), request, best.route(), best.score(), screening.rejected());
    }

    /**
     * Returns the scoring function for this request. It receives all routes that passed the
     * constraints, so a strategy that compares routes with each other can look at the whole set
     * once instead of once per route.
     */
    protected abstract RouteScorer scorer(List<Route> candidates, DeliveryRequest request);

    @FunctionalInterface
    protected interface RouteScorer {
        RouteScore score(Route route);
    }
}
