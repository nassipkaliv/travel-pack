package kz.aitu.sdp.route.constraint;

import java.util.Optional;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;

/**
 * A hard restriction. A route that violates it is out of the race no matter how good its
 * score is — constraints are checked before any strategy starts ranking.
 */
@FunctionalInterface
public interface RouteConstraint {

    /** Empty when the route satisfies the constraint, otherwise the reason it does not. */
    Optional<String> violation(Route route, DeliveryRequest request);
}
