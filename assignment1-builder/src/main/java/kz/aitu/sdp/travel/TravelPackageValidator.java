package kz.aitu.sdp.travel;

import java.util.ArrayList;
import java.util.List;

/** Business rules. One rule = one method. Collects all errors, then throws once. */
final class TravelPackageValidator {

    private final TravelPackage trip;
    private final List<String> errors = new ArrayList<>();

    TravelPackageValidator(TravelPackage trip) {
        this.trip = trip;
    }

    void validate() {
        checkNightsInRange();
        checkHotelIsChosen();
        checkStarsMatchBudget();
        checkExactlyOneTransport();
        checkVisaForInternationalTrip();
        checkFamilyRoomForChildren();
        checkGuestsFitRoom();
        checkFlightForAirportTransfer();
        if (!errors.isEmpty()) {
            throw new InvalidTravelPackageException(errors);
        }
    }

    /** Valid range: 1..30 nights. */
    private void checkNightsInRange() {
        if (trip.nights() < 1 || trip.nights() > 30) {
            errors.add("Nights must be 1..30, got " + trip.nights());
        }
    }

    /** Mandatory parameter. */
    private void checkHotelIsChosen() {
        if (trip.hotel() == null) {
            errors.add("Hotel is required");
        }
    }

    /** One parameter depends on another: hotel stars depend on budget. */
    private void checkStarsMatchBudget() {
        if (trip.hotel() == null) {
            return;
        }
        BudgetLevel budget = trip.budget();
        int stars = trip.hotel().stars();
        if (stars < budget.minStars() || stars > budget.maxStars()) {
            errors.add("%s budget allows %d..%d stars, got %d"
                    .formatted(budget, budget.minStars(), budget.maxStars(), stars));
        }
    }

    /** Mutually exclusive options: exactly one of flight or train. */
    private void checkExactlyOneTransport() {
        if (trip.flight().isPresent() == trip.train().isPresent()) {
            errors.add("Choose exactly one transport: flight or train");
        }
    }

    /** Mandatory under a condition: international trip needs visa support. */
    private void checkVisaForInternationalTrip() {
        if (trip.isInternational() && !trip.hasVisaSupport()) {
            errors.add("International trip requires visa support");
        }
    }

    /** One component requires another: children require a FAMILY room. */
    private void checkFamilyRoomForChildren() {
        if (trip.children() > 0 && trip.room() != RoomType.FAMILY) {
            errors.add("Children require a FAMILY room");
        }
    }

    /** Range depends on another parameter: guests must fit room capacity. */
    private void checkGuestsFitRoom() {
        RoomType room = trip.room();
        if (trip.guests() > room.capacity()) {
            errors.add("%s room fits %d guests, got %d".formatted(room, room.capacity(), trip.guests()));
        }
    }

    /** One component requires another: airport transfer requires a flight. */
    private void checkFlightForAirportTransfer() {
        if (trip.hasAirportTransfer() && trip.flight().isEmpty()) {
            errors.add("Airport transfer requires a flight");
        }
    }
}
