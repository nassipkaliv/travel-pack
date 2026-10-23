package kz.aitu.sdp.route;

import java.util.List;
import java.util.Set;
import kz.aitu.sdp.route.domain.CargoType;
import kz.aitu.sdp.route.domain.DeliveryPriority;
import kz.aitu.sdp.route.domain.DeliveryRequest;
import kz.aitu.sdp.route.domain.Location;
import kz.aitu.sdp.route.domain.Route;
import kz.aitu.sdp.route.domain.TrafficLevel;

/**
 * Eight ways to get an order from the logistics hub to the Expo district in Astana, and the
 * requests the demo plans for.
 */
public final class DemoData {

    public static final Location HUB = new Location("Astana logistics hub", 51.1280, 71.4304);
    public static final Location EXPO = new Location("Expo business district", 51.0890, 71.4160);

    /** URGENT medical parcel: time matters most, money hardly at all. */
    public static final DeliveryRequest URGENT_MEDICAL =
            new DeliveryRequest("101", DeliveryPriority.URGENT, CargoType.MEDICAL, 10_000, 40, false);

    /** ECONOMY parcel: the cheapest way, it may take time. */
    public static final DeliveryRequest ECONOMY_PARCEL =
            new DeliveryRequest("102", DeliveryPriority.ECONOMY, CargoType.GENERAL, 3_000, 90, false);

    /** ECO delivery with an emission cap of 2 kg. */
    public static final DeliveryRequest ECO_PARCEL =
            new DeliveryRequest("103", DeliveryPriority.ECO, CargoType.GENERAL, 5_000, 60, 2.0, true);

    /** STANDARD fragile parcel: a sensible compromise. */
    public static final DeliveryRequest STANDARD_FRAGILE =
            new DeliveryRequest("104", DeliveryPriority.STANDARD, CargoType.FRAGILE, 4_000, 45, false);

    /** The request every strategy is compared on. */
    public static final DeliveryRequest COMPARISON =
            new DeliveryRequest("105", DeliveryPriority.STANDARD, CargoType.GENERAL, 5_000, 60, false);

    /** Hazardous cargo, small budget, tight deadline — nothing fits. */
    public static final DeliveryRequest IMPOSSIBLE =
            new DeliveryRequest("106", DeliveryPriority.URGENT, CargoType.HAZARDOUS, 3_000, 30, false);

    /** URGENT hazardous cargo for a customer who paid for a guaranteed slot. */
    public static final DeliveryRequest PREMIUM_HAZARDOUS =
            new DeliveryRequest("107", DeliveryPriority.URGENT, CargoType.HAZARDOUS, 20_000, 44, false);

    private DemoData() {
    }

    public static List<Route> routes() {
        return List.of(
                route("R1", "Ring road, diesel van", 24, 32, 1500, 6.2, TrafficLevel.MODERATE, true, Set.of()),
                route("R2", "Expressway, motorbike courier", 21, 22, 2600, 3.1, TrafficLevel.LOW, true,
                        Set.of(CargoType.HAZARDOUS, CargoType.FRAGILE)),
                route("R3", "City centre, diesel van", 17, 25, 1400, 4.8, TrafficLevel.HEAVY, true, Set.of()),
                route("R4", "Turan avenue, electric van", 19, 34, 1900, 0.9, TrafficLevel.MODERATE, true, Set.of()),
                route("R5", "Night consolidated truck", 28, 55, 400, 7.5, TrafficLevel.LOW, true, Set.of()),
                route("R6", "City centre, cargo bike", 14, 52, 1100, 0.1, TrafficLevel.LOW, true,
                        Set.of(CargoType.HAZARDOUS)),
                route("R7", "Tunnel shortcut, diesel van", 15, 18, 2200, 4.0, TrafficLevel.HEAVY, true,
                        Set.of(CargoType.HAZARDOUS)),
                route("R8", "Bridge route, diesel van", 22, 28, 1300, 5.5, TrafficLevel.LOW, false, Set.of()));
    }

    private static Route route(String id, String name, double km, int minutes, double cost, double co2,
                               TrafficLevel traffic, boolean available, Set<CargoType> prohibited) {
        return new Route(id, name, HUB, EXPO, km, minutes, cost, co2, traffic, available, prohibited);
    }
}
