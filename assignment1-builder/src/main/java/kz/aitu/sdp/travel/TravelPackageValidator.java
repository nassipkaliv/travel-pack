package kz.aitu.sdp.travel;

import java.util.ArrayList;
import java.util.List;

/** Business rules. One rule = one method. Collects all errors, then throws once. */
final class TravelPackageValidator {

    private final TravelPackage p;
    private final List<String> errors = new ArrayList<>();

    TravelPackageValidator(TravelPackage p) {
        this.p = p;
    }

    void validate() {
        checkNights();
        checkHotel();
        checkTransport();
        checkVisa();
        checkRoom();
        checkAirportTransfer();
        if (!errors.isEmpty()) {
            throw new InvalidTravelPackageException(errors);
        }
    }

    /** Range: 1..30 nights. */
    private void checkNights() {
        if (p.nights() < 1 || p.nights() > 30) {
            errors.add("Nights must be 1..30, got " + p.nights());
        }
    }

    /** Mandatory + depends on another parameter: stars must fit the budget. */
    private void checkHotel() {
        if (p.hotel() == null) {
            errors.add("Hotel is required");
            return;
        }
        BudgetLevel budget = p.budget();
        int stars = p.hotel().stars();
        if (stars < budget.minStars() || stars > budget.maxStars()) {
            errors.add("%s budget allows %d..%d stars, got %d"
                    .formatted(budget, budget.minStars(), budget.maxStars(), stars));
        }
    }

    /** Mutually exclusive: exactly one of flight or train. */
    private void checkTransport() {
        boolean hasFlight = p.flight().isPresent();
        boolean hasTrain = p.train().isPresent();
        if (hasFlight == hasTrain) {
            errors.add("Choose exactly one transport: flight or train");
        }
    }

    /** Mandatory under condition: international trip needs visa support. */
    private void checkVisa() {
        if (p.isInternational() && !p.hasVisaSupport()) {
            errors.add("International trip requires visa support");
        }
    }

    /** Children need a FAMILY room; guests must fit the room. */
    private void checkRoom() {
        if (p.children() > 0 && p.room() != RoomType.FAMILY) {
            errors.add("Children require a FAMILY room");
        }
        if (p.guests() > p.room().capacity()) {
            errors.add("%s room fits %d guests, got %d".formatted(p.room(), p.room().capacity(), p.guests()));
        }
    }

    /** Component requires another component: transfer needs a flight. */
    private void checkAirportTransfer() {
        if (p.hasAirportTransfer() && p.flight().isEmpty()) {
            errors.add("Airport transfer requires a flight");
        }
    }
}
