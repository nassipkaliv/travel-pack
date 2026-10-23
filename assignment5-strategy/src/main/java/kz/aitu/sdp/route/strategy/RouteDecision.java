package kz.aitu.sdp.route.strategy;

import java.util.List;
import kz.aitu.sdp.route.constraint.Rejection;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;

/**
 * The result of a strategy: the chosen route, why it won and why the others were rejected.
 */
public record RouteDecision(
        String strategyName,
        DeliveryRequest request,
        Route route,
        RouteScore score,
        List<Rejection> rejections) {

    public RouteDecision {
        rejections = List.copyOf(rejections);
    }

    public String explain() {
        StringBuilder text = new StringBuilder();
        text.append("Selected: ").append(route).append(System.lineSeparator());
        text.append("Reason:").append(System.lineSeparator());
        score.breakdown().forEach(line -> text.append("  ").append(line).append(System.lineSeparator()));
        rejections.forEach(rejection -> text.append(rejection).append(System.lineSeparator()));
        return text.toString().stripTrailing();
    }
}
