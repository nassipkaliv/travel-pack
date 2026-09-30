package kz.aitu.sdp.trip;

import kz.aitu.sdp.trip.app.TripPlan;
import kz.aitu.sdp.trip.app.TripPlanner;
import kz.aitu.sdp.trip.app.TripRequest;
import kz.aitu.sdp.trip.factory.TripFactory;
import kz.aitu.sdp.trip.factory.TripFactoryProvider;
import kz.aitu.sdp.trip.product.TripTier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Part D: a trip always stays inside one product family. */
class CompatibilityTest {

    private static final TripRequest REQUEST = new TripRequest("Aliya", "Antalya", 3, 2);

    @ParameterizedTest
    @EnumSource(TripTier.class)
    void allComponentsOfAPlanShareOneTier(TripTier tier) {
        TripPlan plan = new TripPlanner(TripFactoryProvider.forTier(tier)).planTrip(REQUEST);

        assertAll(
                () -> assertEquals(tier, plan.tier()),
                () -> assertEquals(tier, plan.transport().tier()),
                () -> assertEquals(tier, plan.stay().tier()),
                () -> assertEquals(tier, plan.excursion().tier()));
    }

    @Test
    void aPlannerCanOnlyProducePlansOfItsOwnFamily() {
        TripPlanner budget = new TripPlanner(TripFactoryProvider.forTier(TripTier.BUDGET));
        TripPlanner premium = new TripPlanner(TripFactoryProvider.forTier(TripTier.PREMIUM));

        assertAll(
                () -> assertEquals(TripTier.BUDGET, budget.planTrip(REQUEST).tier()),
                () -> assertEquals(TripTier.PREMIUM, premium.planTrip(REQUEST).tier()));
    }

    /** Negative scenario: a hand-assembled mixed plan is refused by the safety net. */
    @Test
    void mixingComponentsOfTwoFamiliesIsRejected() {
        TripFactory budget = TripFactoryProvider.forTier(TripTier.BUDGET);
        TripFactory premium = TripFactoryProvider.forTier(TripTier.PREMIUM);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new TripPlan(TripTier.BUDGET, budget.createTransport(), premium.createAccommodation(),
                        budget.createExcursion(), REQUEST));

        assertTrue(error.getMessage().contains("PREMIUM"), error.getMessage());
    }
}
