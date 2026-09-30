package kz.aitu.sdp.trip;

import java.util.List;
import kz.aitu.sdp.trip.app.BudgetExceededException;
import kz.aitu.sdp.trip.app.PackingList;
import kz.aitu.sdp.trip.app.Quote;
import kz.aitu.sdp.trip.app.ScheduleEntry;
import kz.aitu.sdp.trip.app.TripPlan;
import kz.aitu.sdp.trip.app.TripPlanner;
import kz.aitu.sdp.trip.app.TripRequest;
import kz.aitu.sdp.trip.factory.TripFactoryProvider;
import kz.aitu.sdp.trip.product.TripTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Part F: business operations in which the three products work together. */
class TripPlannerTest {

    private static final TripRequest REQUEST = new TripRequest("Aliya", "Antalya", 3, 2);

    private static TripPlanner planner(TripTier tier) {
        return new TripPlanner(TripFactoryProvider.forTier(tier));
    }

    private static String lastActivity(TripPlanner planner) {
        List<ScheduleEntry> schedule = planner.arrivalSchedule(planner.planTrip(REQUEST));
        return schedule.get(schedule.size() - 1).activity();
    }

    private static ScheduleEntry excursionEntry(List<ScheduleEntry> schedule) {
        return schedule.stream()
                .filter(entry -> entry.activity().startsWith("Excursion"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void lateBusArrivalPushesTheBudgetExcursionToTheNextDay() {
        List<ScheduleEntry> schedule = planner(TripTier.BUDGET).arrivalSchedule(planner(TripTier.BUDGET).planTrip(REQUEST));
        ScheduleEntry excursion = excursionEntry(schedule);

        assertAll(
                () -> assertEquals("22:00", schedule.get(1).time(), "arrival after a 14 h bus"),
                () -> assertEquals(2, excursion.day(), "no excursion at midnight"),
                () -> assertEquals("10:00", excursion.time()));
    }

    @Test
    void shortFlightAndEarlyCheckInKeepThePremiumExcursionOnTheArrivalDay() {
        TripPlanner planner = planner(TripTier.PREMIUM);
        ScheduleEntry excursion = excursionEntry(planner.arrivalSchedule(planner.planTrip(REQUEST)));

        assertAll(
                () -> assertEquals(1, excursion.day()),
                () -> assertEquals("13:30", excursion.time()));
    }

    @Test
    void groupTourWaitsForItsFixedStartHour() {
        TripPlanner planner = planner(TripTier.STANDARD);
        ScheduleEntry excursion = excursionEntry(planner.arrivalSchedule(planner.planTrip(REQUEST)));

        assertAll(
                () -> assertEquals(2, excursion.day(), "the 09:00 tour cannot start after check-in at 14:00"),
                () -> assertEquals("09:00", excursion.time()));
    }

    @Test
    void dinnerDependsOnTheMealPlanOfTheStay() {
        TripPlanner budget = planner(TripTier.BUDGET);
        TripPlanner premium = planner(TripTier.PREMIUM);

        String budgetDinner = lastActivity(budget);
        String premiumDinner = lastActivity(premium);

        assertAll(
                () -> assertTrue(budgetDinner.contains("local cafe"), budgetDinner),
                () -> assertTrue(premiumDinner.contains("ALL_INCLUSIVE"), premiumDinner));
    }

    @Test
    void quoteCountsNightsAndTravelersSeparately() {
        TripPlanner planner = planner(TripTier.STANDARD);
        Quote quote = planner.quote(planner.planTrip(REQUEST));

        assertAll(
                () -> assertEquals(60_000 * 2, quote.transportCost()),
                () -> assertEquals(25_000 * 3, quote.accommodationCost()),
                () -> assertEquals(12_000 * 2, quote.excursionCost()),
                () -> assertEquals(219_000, quote.total()),
                () -> assertEquals(109_500, quote.perPerson()));
    }

    @Test
    void packingAdviceCombinesAllThreeProducts() {
        TripPlanner planner = planner(TripTier.BUDGET);
        PackingList packing = planner.packingAdvice(planner.planTrip(REQUEST));

        assertAll(
                () -> assertEquals(10, packing.baggageLimitKg(), "from the transport"),
                () -> assertTrue(packing.items().contains("padlock"), "from the stay"),
                () -> assertTrue(packing.items().contains("walking shoes"), "from the excursion"),
                () -> assertTrue(packing.advice().stream().anyMatch(note -> note.contains("pack light"))),
                () -> assertTrue(packing.advice().stream().anyMatch(note -> note.contains("No meals"))));
    }

    @Test
    void groupTourAdvisesToArriveEarlyWhilePrivateGuideDoesNot() {
        TripPlanner standard = planner(TripTier.STANDARD);
        TripPlanner premium = planner(TripTier.PREMIUM);

        List<String> standardAdvice = standard.packingAdvice(standard.planTrip(REQUEST)).advice();
        List<String> premiumAdvice = premium.packingAdvice(premium.planTrip(REQUEST)).advice();

        assertAll(
                () -> assertTrue(standardAdvice.stream().anyMatch(note -> note.contains("15 minutes"))),
                () -> assertTrue(premiumAdvice.stream().noneMatch(note -> note.contains("15 minutes"))));
    }

    @Test
    void cheapFamilyFitsASmallBudget() {
        assertDoesNotThrow(() -> planner(TripTier.BUDGET).planWithinBudget(REQUEST, 20_000));
    }

    /** Negative scenario. */
    @Test
    void expensiveFamilyIsRejectedWhenTheBudgetIsTooSmall() {
        BudgetExceededException error = assertThrows(BudgetExceededException.class,
                () -> planner(TripTier.PREMIUM).planWithinBudget(REQUEST, 100_000));

        assertTrue(error.getMessage().contains("PREMIUM"), error.getMessage());
    }

    /** Negative scenario. */
    @Test
    void requestWithoutNightsIsRejected() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new TripRequest("Aliya", "Antalya", 0, 2)),
                () -> assertThrows(IllegalArgumentException.class, () -> new TripRequest("Aliya", "Antalya", 3, 0)),
                () -> assertThrows(IllegalArgumentException.class, () -> new TripRequest(" ", "Antalya", 3, 2)));
    }

    @Test
    void planKeepsTheRequestItWasBuiltFrom() {
        TripPlan plan = planner(TripTier.STANDARD).planTrip(REQUEST);

        assertAll(
                () -> assertEquals(3, plan.nights()),
                () -> assertEquals(2, plan.travelers()),
                () -> assertEquals("Antalya", plan.request().city()));
    }
}
