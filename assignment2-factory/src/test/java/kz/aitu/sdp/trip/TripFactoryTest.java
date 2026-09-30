package kz.aitu.sdp.trip;

import java.lang.reflect.Modifier;
import kz.aitu.sdp.trip.factory.TripFactory;
import kz.aitu.sdp.trip.factory.TripFactoryProvider;
import kz.aitu.sdp.trip.product.MealPlan;
import kz.aitu.sdp.trip.product.TripComponent;
import kz.aitu.sdp.trip.product.TripTier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Creation of every family and of the right concrete products. */
class TripFactoryTest {

    private static String nameOfTransport(TripTier tier) {
        return TripFactoryProvider.forTier(tier).createTransport().getClass().getSimpleName();
    }

    @ParameterizedTest
    @EnumSource(TripTier.class)
    void everyFamilyCreatesAllThreeProductTypes(TripTier tier) {
        TripFactory factory = TripFactoryProvider.forTier(tier);

        assertAll(
                () -> assertEquals(tier, factory.createTransport().tier()),
                () -> assertEquals(tier, factory.createAccommodation().tier()),
                () -> assertEquals(tier, factory.createExcursion().tier()));
    }

    @Test
    void budgetFactoryCreatesBusHostelAndSelfGuidedWalk() {
        TripFactory factory = TripFactoryProvider.forTier(TripTier.BUDGET);

        assertAll(
                () -> assertEquals("BusTransfer", factory.createTransport().getClass().getSimpleName()),
                () -> assertEquals("Hostel", factory.createAccommodation().getClass().getSimpleName()),
                () -> assertEquals("SelfGuidedWalk", factory.createExcursion().getClass().getSimpleName()));
    }

    @Test
    void standardFactoryCreatesFlightHotelAndGroupTour() {
        TripFactory factory = TripFactoryProvider.forTier(TripTier.STANDARD);

        assertAll(
                () -> assertEquals("EconomyFlight", factory.createTransport().getClass().getSimpleName()),
                () -> assertEquals("Hotel3Star", factory.createAccommodation().getClass().getSimpleName()),
                () -> assertEquals("GroupTour", factory.createExcursion().getClass().getSimpleName()));
    }

    @Test
    void premiumFactoryCreatesBusinessFlightResortAndPrivateGuide() {
        TripFactory factory = TripFactoryProvider.forTier(TripTier.PREMIUM);

        assertAll(
                () -> assertEquals("BusinessFlight", factory.createTransport().getClass().getSimpleName()),
                () -> assertEquals("Resort5Star", factory.createAccommodation().getClass().getSimpleName()),
                () -> assertEquals("PrivateGuide", factory.createExcursion().getClass().getSimpleName()));
    }

    @Test
    void behaviourDiffersBetweenFamilies() {
        TripFactory budget = TripFactoryProvider.forTier(TripTier.BUDGET);
        TripFactory premium = TripFactoryProvider.forTier(TripTier.PREMIUM);

        assertAll(
                () -> assertTrue(budget.createTransport().travelTimeMinutes()
                        > premium.createTransport().travelTimeMinutes() * 5),
                () -> assertEquals(MealPlan.NONE, budget.createAccommodation().meals()),
                () -> assertEquals(MealPlan.ALL_INCLUSIVE, premium.createAccommodation().meals()),
                () -> assertFalse(budget.createAccommodation().privateRoom()),
                () -> assertTrue(premium.createAccommodation().privateRoom()));
    }

    /** Part D: outside code cannot even name a concrete product, so it cannot instantiate one. */
    @ParameterizedTest
    @EnumSource(TripTier.class)
    void concreteProductsAreNotVisibleOutsideTheirFamilyPackage(TripTier tier) {
        TripFactory factory = TripFactoryProvider.forTier(tier);

        assertAll(
                () -> assertNotPublic(factory.createTransport()),
                () -> assertNotPublic(factory.createAccommodation()),
                () -> assertNotPublic(factory.createExcursion()));
    }

    @Test
    void factoriesOfDifferentFamiliesProduceDifferentClasses() {
        assertAll(
                () -> assertFalse(nameOfTransport(TripTier.BUDGET).equals(nameOfTransport(TripTier.STANDARD))),
                () -> assertFalse(nameOfTransport(TripTier.STANDARD).equals(nameOfTransport(TripTier.PREMIUM))));
    }

    private static void assertNotPublic(TripComponent component) {
        assertFalse(Modifier.isPublic(component.getClass().getModifiers()),
                component.getClass().getName() + " must not be public");
    }
}
