package kz.aitu.sdp.route.constraint;

import java.util.List;
import kz.aitu.sdp.route.domain.Route;

/** The candidate routes split into those a strategy may rank and those it may not. */
public record Screening(List<Route> accepted, List<Rejection> rejected) {

    public Screening {
        accepted = List.copyOf(accepted);
        rejected = List.copyOf(rejected);
    }
}
