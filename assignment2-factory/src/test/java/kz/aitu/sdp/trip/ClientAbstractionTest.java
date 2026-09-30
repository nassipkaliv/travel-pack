package kz.aitu.sdp.trip;

import java.lang.reflect.Constructor;
import kz.aitu.sdp.trip.app.TripPlan;
import kz.aitu.sdp.trip.app.TripPlanner;
import kz.aitu.sdp.trip.app.TripRequest;
import kz.aitu.sdp.trip.factory.TripFactory;
import kz.aitu.sdp.trip.factory.TripFactoryProvider;
import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Proof that the client side works through abstractions only. */
class ClientAbstractionTest {

    private static final TripRequest REQUEST = new TripRequest("Aliya", "Antalya", 2, 2);

    @Test
    void plannerAcceptsTheFactoryInterfaceAndNotAConcreteFactory() {
        Constructor<?>[] constructors = TripPlanner.class.getConstructors();

        assertAll(
                () -> assertEquals(1, constructors.length),
                () -> assertEquals(TripFactory.class, constructors[0].getParameterTypes()[0]),
                () -> assertTrue(TripFactory.class.isInterface()));
    }

    @Test
    void oneClientLoopHandlesEveryRegisteredFamily() {
        for (TripTier tier : TripFactoryProvider.supportedTiers()) {
            TripPlan plan = new TripPlanner(TripFactoryProvider.forTier(tier)).planTrip(REQUEST);

            assertAll(
                    () -> assertInstanceOf(Transport.class, plan.transport()),
                    () -> assertInstanceOf(Accommodation.class, plan.stay()),
                    () -> assertInstanceOf(Excursion.class, plan.excursion()),
                    () -> assertEquals(tier, plan.tier()));
        }
    }

    @Test
    void addingAFamilyDoesNotChangeTheClientCode() {
        int families = TripFactoryProvider.supportedTiers().size();

        assertEquals(TripTier.values().length, families,
                "every tier of the enum must have a registered factory");
    }
}
