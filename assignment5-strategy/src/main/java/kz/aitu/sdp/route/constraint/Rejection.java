package kz.aitu.sdp.route.constraint;

import java.util.List;
import kz.aitu.sdp.route.domain.Route;

/** A route that broke at least one hard constraint, with every reason it broke. */
public record Rejection(Route route, List<String> reasons) {

    public Rejection {
        reasons = List.copyOf(reasons);
    }

    @Override
    public String toString() {
        return route.id() + " rejected: " + String.join("; ", reasons);
    }
}
