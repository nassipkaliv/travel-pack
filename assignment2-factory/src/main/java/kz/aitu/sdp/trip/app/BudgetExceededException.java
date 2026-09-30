package kz.aitu.sdp.trip.app;

import kz.aitu.sdp.trip.product.TripTier;

/** Thrown when the selected family is too expensive for the traveler's limit. */
public class BudgetExceededException extends RuntimeException {

    public BudgetExceededException(TripTier tier, int perPerson, int maxPerPerson) {
        super("%s trip costs %d KZT per person, limit is %d KZT".formatted(tier, perPerson, maxPerPerson));
    }
}
