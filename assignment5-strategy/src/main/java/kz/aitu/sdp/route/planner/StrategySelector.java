package kz.aitu.sdp.route.planner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.strategy.RouteStrategy;

/**
 * Decides <em>which</em> strategy serves a request; the strategy then decides <em>how</em>
 * to choose the route. Two questions, two classes.
 *
 * <p>The mapping is data, not code: an ordered list of rules "condition -> strategy", checked
 * top to bottom, first match wins, with a fallback when nothing matches. A new business rule is
 * one more {@link #when} call where the selector is configured — this class does not change and
 * has no switch to grow.
 */
public final class StrategySelector {

    private final List<SelectionRule> rules = new ArrayList<>();
    private final RouteStrategy fallback;

    public StrategySelector(RouteStrategy fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback strategy is required");
    }

    /** Adds a rule after the existing ones. Returns this selector for chaining. */
    public StrategySelector when(String description, Predicate<DeliveryRequest> condition, RouteStrategy strategy) {
        rules.add(new SelectionRule(description, condition, strategy));
        return this;
    }

    public RouteStrategy select(DeliveryRequest request) {
        return choose(request).strategy();
    }

    /** The chosen strategy together with the rule that chose it. */
    public Selection choose(DeliveryRequest request) {
        Objects.requireNonNull(request, "delivery request is required");
        return rules.stream()
                .filter(rule -> rule.condition().test(request))
                .findFirst()
                .map(rule -> new Selection(rule.description(), rule.strategy()))
                .orElseGet(() -> new Selection("no rule matched, fallback", fallback));
    }

    public record Selection(String rule, RouteStrategy strategy) {
    }

    private record SelectionRule(String description, Predicate<DeliveryRequest> condition, RouteStrategy strategy) {

        SelectionRule {
            Objects.requireNonNull(description, "rule description is required");
            Objects.requireNonNull(condition, "rule condition is required");
            Objects.requireNonNull(strategy, "rule strategy is required");
        }
    }
}
