package kz.aitu.sdp.route.strategy;

import java.util.List;

/**
 * How a strategy rated one route. Lower is better. {@code breakdown} lists the steps that led
 * to {@code value}, in the order a person would check them.
 */
public record RouteScore(double value, List<String> breakdown) {

    public RouteScore {
        breakdown = List.copyOf(breakdown);
    }
}
