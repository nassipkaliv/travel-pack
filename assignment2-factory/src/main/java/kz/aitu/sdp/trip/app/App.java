package kz.aitu.sdp.trip.app;

import kz.aitu.sdp.trip.booking.Booking;
import kz.aitu.sdp.trip.booking.BookingChannel;
import kz.aitu.sdp.trip.booking.BookingChannels;
import kz.aitu.sdp.trip.booking.Customer;
import kz.aitu.sdp.trip.config.ExternalConfiguration;
import kz.aitu.sdp.trip.factory.TripFactory;
import kz.aitu.sdp.trip.factory.TripFactoryProvider;

/**
 * Client of the system.
 *
 * <p>It picks a family and a channel once, from the configuration, and then works only with
 * {@link TripFactory}, {@link TripPlanner} and {@link BookingChannel}. Nothing below this
 * line knows which family is running.
 *
 * <pre>
 * java -cp target/classes kz.aitu.sdp.trip.app.App --family=PREMIUM --channel=agency --nights=5
 * </pre>
 */
public final class App {

    public static void main(String[] args) {
        ExternalConfiguration configuration = ExternalConfiguration.fromRuntime(args);
        TripFactory factory = TripFactoryProvider.from(configuration);
        BookingChannel channel = BookingChannels.from(configuration);

        TripRequest request = requestFrom(configuration);
        TripPlanner planner = new TripPlanner(factory);
        TripPlan plan = planner.planTrip(request);
        Quote quote = planner.quote(plan);

        System.out.printf("Family selected at runtime: %s, booking channel: %s%n%n", planner.tier(), channel.name());
        printPlan(plan, quote);
        printSchedule(planner, plan);
        printPacking(planner, plan);
        printBooking(channel, plan, quote, request);
    }

    private static TripRequest requestFrom(ExternalConfiguration configuration) {
        String city = configuration.value("city", "TRIP_CITY", "city").orElse("Antalya");
        int nights = configuration.value("nights", "TRIP_NIGHTS", "nights").map(Integer::parseInt).orElse(5);
        int travelers = configuration.value("travelers", "TRIP_TRAVELERS", "travelers")
                .map(Integer::parseInt).orElse(2);
        return new TripRequest("Aliya", city, nights, travelers);
    }

    private static void printPlan(TripPlan plan, Quote quote) {
        System.out.println("Trip to " + plan.request().city());
        System.out.println("  Transport:  " + plan.transport().describe());
        System.out.println("  Stay:       " + plan.stay().describe());
        System.out.println("  Excursion:  " + plan.excursion().describe());
        System.out.printf("  Price:      %d KZT total, %d KZT per person%n%n", quote.total(), quote.perPerson());
    }

    private static void printSchedule(TripPlanner planner, TripPlan plan) {
        System.out.println("Arrival day");
        planner.arrivalSchedule(plan).forEach(entry -> System.out.println("  " + entry));
        System.out.println();
    }

    private static void printPacking(TripPlanner planner, TripPlan plan) {
        PackingList packing = planner.packingAdvice(plan);
        System.out.printf("Packing (baggage limit %d kg)%n", packing.baggageLimitKg());
        packing.items().forEach(item -> System.out.println("  - " + item));
        packing.advice().forEach(note -> System.out.println("  ! " + note));
        System.out.println();
    }

    private static void printBooking(BookingChannel channel, TripPlan plan, Quote quote, TripRequest request) {
        Customer customer = channel.name().equals("Corporate")
                ? new Customer(request.travelerName(), request.travelers(), "aliya@company.kz", "AITU LLP")
                : Customer.person(request.travelerName(), request.travelers(), "aliya@example.com");
        Booking booking = channel.book(plan, quote, customer);
        System.out.println(channel.confirmationMessage(booking));
        System.out.println();
        System.out.println(booking.document().render());
    }
}
