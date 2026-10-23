package kz.aitu.sdp.route;

import java.util.Set;
import kz.aitu.sdp.route.domain.CargoType;
import kz.aitu.sdp.route.domain.DeliveryPriority;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.TrafficLevel;

/** Small hand-made routes, so every expected number in a test can be checked by hand. */
final class Fixtures {

    private Fixtures() {
    }

    static Route route(String id, double km, int minutes, double cost, double co2, TrafficLevel traffic) {
        return new Route(id, id, DemoData.HUB, DemoData.EXPO, km, minutes, cost, co2, traffic, true, Set.of());
    }

    static Route unavailable(String id, double km, int minutes, double cost, double co2) {
        return new Route(id, id, DemoData.HUB, DemoData.EXPO, km, minutes, cost, co2, TrafficLevel.LOW, false, Set.of());
    }

    static Route prohibiting(CargoType cargo, String id, double km, int minutes, double cost, double co2) {
        return new Route(id, id, DemoData.HUB, DemoData.EXPO, km, minutes, cost, co2, TrafficLevel.LOW, true, Set.of(cargo));
    }

    /** STANDARD priority adds a 300 KZT surcharge; GENERAL cargo adds no fee. */
    static DeliveryRequest request(double budget, int deadline) {
        return new DeliveryRequest("T1", DeliveryPriority.STANDARD, CargoType.GENERAL, budget, deadline, false);
    }
}
