package kz.aitu.sdp.trip.app;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import kz.aitu.sdp.trip.factory.TripFactory;
import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.MealPlan;
import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

/**
 * The client of the Abstract Factory (Parts D and F).
 *
 * <p>It knows the product interfaces and one {@link TripFactory}; it never names a concrete
 * product or a concrete factory. Because all three components come from the same factory
 * instance, every plan it produces belongs to exactly one family.
 */
public final class TripPlanner {

    private static final int DEPARTURE_MINUTES = 8 * 60;
    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final int READY_AFTER_CHECK_IN_MINUTES = 90;
    private static final int FLEXIBLE_EARLIEST_HOUR = 8;
    private static final int FLEXIBLE_LATEST_HOUR = 18;
    private static final int NEXT_DAY_START_HOUR = 10;

    private final TripFactory factory;

    public TripPlanner(TripFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public TripTier tier() {
        return factory.tier();
    }

    /** Business operation 1: assemble a trip from one family. */
    public TripPlan planTrip(TripRequest request) {
        return new TripPlan(factory.tier(),
                factory.createTransport(),
                factory.createAccommodation(),
                factory.createExcursion(),
                request);
    }

    /** Business operation 1b: the same, but refuse a family the traveler cannot afford. */
    public TripPlan planWithinBudget(TripRequest request, int maxPerPerson) {
        TripPlan plan = planTrip(request);
        Quote quote = quote(plan);
        if (!quote.fitsBudget(maxPerPerson)) {
            throw new BudgetExceededException(plan.tier(), quote.perPerson(), maxPerPerson);
        }
        return plan;
    }

    /** Business operation 2: price breakdown of a plan. */
    public Quote quote(TripPlan plan) {
        int travelers = plan.travelers();
        return new Quote(
                plan.transport().pricePerPerson() * travelers,
                plan.stay().pricePerNight() * plan.nights(),
                plan.excursion().pricePerPerson() * travelers,
                travelers);
    }

    /**
     * Business operation 3: the itinerary of the arrival day.
     *
     * <p>The three products collaborate here: travel time decides when the traveler arrives,
     * the check-in hour of the stay decides when the room is available, and the excursion
     * starts only once the traveler has checked in — on the next day if it is too late.
     */
    public List<ScheduleEntry> arrivalSchedule(TripPlan plan) {
        Transport transport = plan.transport();
        Accommodation stay = plan.stay();
        Excursion excursion = plan.excursion();

        int arrival = DEPARTURE_MINUTES + transport.travelTimeMinutes();
        int checkIn = checkInTime(arrival, stay);
        int excursionStart = excursionStart(checkIn + READY_AFTER_CHECK_IN_MINUTES, excursion);

        List<ScheduleEntry> schedule = new ArrayList<>();
        schedule.add(new ScheduleEntry(DEPARTURE_MINUTES, "Departure: " + transport.describe()));
        schedule.add(new ScheduleEntry(arrival, arrivalActivity(transport)));
        schedule.add(new ScheduleEntry(checkIn, checkInActivity(stay, checkIn > arrival)));
        schedule.add(new ScheduleEntry(excursionStart, "Excursion: %s (guide: %s)"
                .formatted(excursion.describe(), excursion.guide())));
        schedule.add(new ScheduleEntry(excursionStart + excursion.durationMinutes(), dinnerActivity(stay)));
        return List.copyOf(schedule);
    }

    /**
     * Business operation 4: what to pack.
     *
     * <p>Combines the baggage limit of the transport with the needs of the stay and of the
     * excursion, so the advice differs from family to family.
     */
    public PackingList packingAdvice(TripPlan plan) {
        Transport transport = plan.transport();
        Accommodation stay = plan.stay();
        Excursion excursion = plan.excursion();

        Set<String> items = new LinkedHashSet<>(stay.packingItems());
        items.addAll(excursion.packingItems());

        List<String> advice = new ArrayList<>();
        if (transport.baggageKg() <= 10) {
            advice.add("Only %d kg of baggage: pack light".formatted(transport.baggageKg()));
        }
        if (transport.doorToDoor()) {
            advice.add("Door-to-door transfer: no transfer to arrange");
        }
        if (stay.meals() == MealPlan.NONE) {
            advice.add("No meals included: budget for food");
        }
        if (!stay.privateRoom()) {
            advice.add("Shared room: keep valuables locked");
        }
        if (excursion.groupSize() > 10) {
            advice.add("Group of %d: arrive 15 minutes before the start".formatted(excursion.groupSize()));
        }
        if (excursion.flexibleStart()) {
            advice.add("Excursion time is flexible: agree it on arrival");
        }
        return new PackingList(transport.baggageKg(), List.copyOf(items), advice);
    }

    private static int checkInTime(int arrival, Accommodation stay) {
        int opensAt = arrival / MINUTES_PER_DAY * MINUTES_PER_DAY + stay.checkInHour() * 60;
        return Math.max(arrival, opensAt);
    }

    private static int excursionStart(int readyAt, Excursion excursion) {
        if (!excursion.flexibleStart()) {
            return nextOccurrence(readyAt, excursion.startHour());
        }
        int hour = readyAt % MINUTES_PER_DAY / 60;
        boolean withinDaylight = hour >= FLEXIBLE_EARLIEST_HOUR && hour < FLEXIBLE_LATEST_HOUR;
        return withinDaylight ? readyAt : nextOccurrence(readyAt, NEXT_DAY_START_HOUR);
    }

    private static int nextOccurrence(int notBefore, int hour) {
        int candidate = notBefore / MINUTES_PER_DAY * MINUTES_PER_DAY + hour * 60;
        return candidate >= notBefore ? candidate : candidate + MINUTES_PER_DAY;
    }

    private static String arrivalActivity(Transport transport) {
        String suffix = transport.doorToDoor() ? "dropped at the door" : "transfer to the city";
        return "Arrival after %d h (%s)".formatted(transport.travelTimeMinutes() / 60, suffix);
    }

    private static String checkInActivity(Accommodation stay, boolean waited) {
        String prefix = waited ? "Check-in opens: " : "Check-in: ";
        return prefix + stay.describe();
    }

    private static String dinnerActivity(Accommodation stay) {
        return stay.meals() == MealPlan.NONE
                ? "Dinner in a local cafe (no meals included)"
                : "Dinner at the stay (%s)".formatted(stay.meals());
    }
}
