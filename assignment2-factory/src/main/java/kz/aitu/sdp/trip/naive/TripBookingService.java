package kz.aitu.sdp.trip.naive;

/**
 * Version WITHOUT Factory Method and Abstract Factory (Part A of the assignment).
 *
 * <p>The client creates concrete products itself. Every problem this causes is marked
 * with a PROBLEM comment below and explained in docs/PART-A-PROBLEMS.md.
 */
public class TripBookingService {

    /**
     * PROBLEM 1: the client depends on all 9 concrete classes.
     * PROBLEM 2: the same if/else over the tier is duplicated for every product type.
     * PROBLEM 3: adding a new tier (e.g. LUXURY) means editing this method — and every
     *            other place in the code that creates products the same way.
     */
    public String bookTrip(String tier, int nights, int travelers) {
        Transport transport;
        if (tier.equals("BUDGET")) {
            transport = new BusTransfer();
        } else if (tier.equals("STANDARD")) {
            transport = new EconomyFlight();
        } else if (tier.equals("PREMIUM")) {
            transport = new BusinessFlight();
        } else {
            throw new IllegalArgumentException("Unknown tier: " + tier);
        }

        Accommodation accommodation;
        if (tier.equals("BUDGET")) {
            accommodation = new Hostel();
        } else if (tier.equals("STANDARD")) {
            accommodation = new Hotel3Star();
        } else if (tier.equals("PREMIUM")) {
            accommodation = new Resort5Star();
        } else {
            throw new IllegalArgumentException("Unknown tier: " + tier);
        }

        Excursion excursion;
        if (tier.equals("BUDGET")) {
            excursion = new SelfGuidedWalk();
        } else if (tier.equals("STANDARD")) {
            excursion = new GroupTour();
        } else if (tier.equals("PREMIUM")) {
            excursion = new PrivateGuide();
        } else {
            throw new IllegalArgumentException("Unknown tier: " + tier);
        }

        return itinerary(tier, transport, accommodation, excursion, nights, travelers);
    }

    /**
     * PROBLEM 4: nothing stops the client from mixing products of different tiers.
     * This method compiles and runs, and produces a nonsense trip:
     * a business class flight together with a bed in a shared hostel room.
     */
    public String bookMixedTrip(int nights, int travelers) {
        return itinerary("MIXED", new BusinessFlight(), new Hostel(), new SelfGuidedWalk(), nights, travelers);
    }

    private String itinerary(String tier, Transport transport, Accommodation accommodation,
                             Excursion excursion, int nights, int travelers) {
        int total = (transport.pricePerPerson() + excursion.pricePerPerson()) * travelers
                + accommodation.pricePerNight() * nights;
        return """
                %s trip, %d nights, %d travelers
                  Transport:  %s (%d h, %d kg baggage)
                  Stay:       %s (meals: %s)
                  Excursion:  %s (guide: %s)
                  Total:      %d KZT"""
                .formatted(tier, nights, travelers,
                        transport.describe(), transport.travelTimeMinutes() / 60, transport.baggageKg(),
                        accommodation.describe(), accommodation.mealsIncluded(),
                        excursion.describe(), excursion.guide(),
                        total);
    }
}
