package kz.aitu.sdp.trip;

import java.util.List;
import kz.aitu.sdp.trip.app.PackingList;
import kz.aitu.sdp.trip.app.ScheduleEntry;
import kz.aitu.sdp.trip.app.TripPlan;
import kz.aitu.sdp.trip.app.TripPlanner;
import kz.aitu.sdp.trip.app.TripRequest;
import kz.aitu.sdp.trip.booking.Booking;
import kz.aitu.sdp.trip.booking.Customer;
import kz.aitu.sdp.trip.booking.OnlineBookingChannel;
import kz.aitu.sdp.trip.config.ExternalConfiguration;
import kz.aitu.sdp.trip.factory.TripFactory;
import kz.aitu.sdp.trip.factory.TripFactoryProvider;
import kz.aitu.sdp.trip.product.MealPlan;
import kz.aitu.sdp.trip.product.TripTier;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Part G: the fourth family works with the business logic that was written before it existed. */
class LuxuryFamilyTest {

    private static final TripRequest REQUEST = new TripRequest("Aliya", "Antalya", 3, 2);

    private static TripPlanner planner() {
        return new TripPlanner(TripFactoryProvider.forTier(TripTier.LUXURY));
    }

    @Test
    void luxuryFactoryCreatesItsOwnThreeProducts() {
        TripFactory factory = TripFactoryProvider.forTier(TripTier.LUXURY);

        assertAll(
                () -> assertEquals("PrivateJet", factory.createTransport().getClass().getSimpleName()),
                () -> assertEquals("PrivateVilla", factory.createAccommodation().getClass().getSimpleName()),
                () -> assertEquals("HelicopterTour", factory.createExcursion().getClass().getSimpleName()),
                () -> assertEquals(MealPlan.ALL_INCLUSIVE, factory.createAccommodation().meals()));
    }

    @Test
    void luxuryPlanIsConsistent() {
        TripPlan plan = planner().planTrip(REQUEST);

        assertAll(
                () -> assertEquals(TripTier.LUXURY, plan.tier()),
                () -> assertEquals(TripTier.LUXURY, plan.transport().tier()),
                () -> assertEquals(TripTier.LUXURY, plan.stay().tier()),
                () -> assertEquals(TripTier.LUXURY, plan.excursion().tier()));
    }

    @Test
    void unchangedSchedulingLogicHandlesTheNewFamily() {
        TripPlanner planner = planner();
        List<ScheduleEntry> schedule = planner.arrivalSchedule(planner.planTrip(REQUEST));
        ScheduleEntry excursion = schedule.stream()
                .filter(entry -> entry.activity().startsWith("Excursion"))
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertEquals("09:30", schedule.get(1).time(), "90 minutes by jet"),
                () -> assertEquals(1, excursion.day()),
                () -> assertEquals("11:00", excursion.time()));
    }

    @Test
    void unchangedPackingLogicHandlesTheNewFamily() {
        TripPlanner planner = planner();
        PackingList packing = planner.packingAdvice(planner.planTrip(REQUEST));

        assertAll(
                () -> assertEquals(100, packing.baggageLimitKg()),
                () -> assertTrue(packing.items().contains("evening outfit")),
                () -> assertTrue(packing.advice().stream().anyMatch(note -> note.contains("Door-to-door"))));
    }

    @Test
    void unchangedBookingLogicHandlesTheNewFamily() {
        TripPlanner planner = planner();
        TripPlan plan = planner.planTrip(REQUEST);

        Booking booking = new OnlineBookingChannel()
                .book(plan, planner.quote(plan), Customer.person("Aliya", 2, "aliya@example.com"));

        assertAll(
                () -> assertTrue(booking.document().render().contains("Private jet")),
                () -> assertTrue(booking.pricePaid() > 0));
    }

    @Test
    void newFamilyCanBeSelectedAtRuntime() {
        ExternalConfiguration configuration =
                new ExternalConfiguration(new String[] {"--family=LUXURY"}, name -> null, new Properties());

        assertAll(
                () -> assertEquals(TripTier.LUXURY, TripFactoryProvider.from(configuration).tier()),
                () -> assertTrue(TripFactoryProvider.supportedTiers().contains(TripTier.LUXURY)));
    }
}
