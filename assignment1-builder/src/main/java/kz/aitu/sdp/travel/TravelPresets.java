package kz.aitu.sdp.travel;

/** Presets return a Builder, so the caller can still add or change anything before build(). */
public final class TravelPresets {

    private TravelPresets() {
    }

    /** 7 nights, premium, all inclusive, airport transfer. Caller adds hotel and flight. */
    public static TravelPackage.Builder allInclusiveBeach(String id, Location from, Location to) {
        return TravelPackage.builder(id, from, to)
                .nights(7)
                .budget(BudgetLevel.PREMIUM)
                .meals(MealPlan.ALL_INCLUSIVE)
                .airportTransfer();
    }
}
