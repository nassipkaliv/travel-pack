package kz.aitu.sdp.route.constraint;

import java.util.List;
import kz.aitu.sdp.route.domain.DeliveryRequest;

/**
 * Thrown when no candidate route may be used for a request.
 *
 * <p>Returning {@code null} would push the question "why?" onto every caller. The exception
 * carries the answer: the request and every rejected route with its reasons, so a dispatcher
 * can relax the budget, move the deadline or put the order on hold.
 */
public class NoSuitableRouteException extends RuntimeException {

    private final transient DeliveryRequest request;
    private final transient List<Rejection> rejections;

    public NoSuitableRouteException(String strategyName, DeliveryRequest request, List<Rejection> rejections) {
        super(message(strategyName, request, rejections));
        this.request = request;
        this.rejections = List.copyOf(rejections);
    }

    public DeliveryRequest request() {
        return request;
    }

    public List<Rejection> rejections() {
        return rejections;
    }

    private static String message(String strategyName, DeliveryRequest request, List<Rejection> rejections) {
        String head = strategyName + " found no suitable route for request #" + request.id();
        if (rejections.isEmpty()) {
            return head + ": no candidate routes were supplied";
        }
        StringBuilder text = new StringBuilder(head).append(':');
        rejections.forEach(rejection -> text.append(System.lineSeparator()).append("  ").append(rejection));
        return text.toString();
    }
}
