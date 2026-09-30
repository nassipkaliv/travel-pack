package kz.aitu.sdp.trip;

import java.util.Map;
import java.util.Properties;
import kz.aitu.sdp.trip.booking.BookingChannel;
import kz.aitu.sdp.trip.booking.BookingChannels;
import kz.aitu.sdp.trip.config.ExternalConfiguration;
import kz.aitu.sdp.trip.factory.TripFactoryProvider;
import kz.aitu.sdp.trip.product.TripTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Part E: the family and the channel come from outside the code. */
class RuntimeSelectionTest {

    private static ExternalConfiguration configuration(String[] args, Map<String, String> environment,
                                                       Map<String, String> fileValues) {
        Properties properties = new Properties();
        fileValues.forEach(properties::setProperty);
        return new ExternalConfiguration(args, environment::get, properties);
    }

    @Test
    void commandLineArgumentWins() {
        ExternalConfiguration configuration = configuration(
                new String[] {"--family=PREMIUM"},
                Map.of("TRIP_FAMILY", "BUDGET"),
                Map.of("family", "STANDARD"));

        assertEquals(TripTier.PREMIUM, TripFactoryProvider.resolveTier(configuration));
    }

    @Test
    void environmentVariableWinsOverTheFile() {
        ExternalConfiguration configuration = configuration(
                new String[0], Map.of("TRIP_FAMILY", "BUDGET"), Map.of("family", "STANDARD"));

        assertEquals(TripTier.BUDGET, TripFactoryProvider.resolveTier(configuration));
    }

    @Test
    void propertiesFileIsUsedWhenNothingElseIsGiven() {
        ExternalConfiguration configuration = configuration(new String[0], Map.of(), Map.of("family", "BUDGET"));

        assertEquals(TripTier.BUDGET, TripFactoryProvider.resolveTier(configuration));
    }

    @Test
    void defaultFamilyIsUsedWhenNoSourceHasAValue() {
        ExternalConfiguration configuration = configuration(new String[0], Map.of(), Map.of());

        assertEquals(TripFactoryProvider.DEFAULT_TIER, TripFactoryProvider.resolveTier(configuration));
    }

    @Test
    void selectedFactoryBuildsTheRequestedFamily() {
        ExternalConfiguration configuration = configuration(new String[] {"--family=budget"}, Map.of(), Map.of());

        assertEquals(TripTier.BUDGET, TripFactoryProvider.from(configuration).tier());
    }

    /** Negative scenario: an unknown family is reported instead of silently falling back. */
    @Test
    void unknownFamilyIsRejected() {
        ExternalConfiguration configuration = configuration(new String[] {"--family=GOLD"}, Map.of(), Map.of());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> TripFactoryProvider.resolveTier(configuration));

        assertTrue(error.getMessage().contains("GOLD"), error.getMessage());
    }

    @Test
    void bookingChannelIsAlsoSelectedAtRuntime() {
        ExternalConfiguration configuration = configuration(new String[] {"--channel=agency"}, Map.of(), Map.of());

        BookingChannel channel = BookingChannels.from(configuration);

        assertEquals("Agency", channel.name());
    }

    @Test
    void defaultChannelIsOnline() {
        ExternalConfiguration configuration = configuration(new String[0], Map.of(), Map.of());

        assertEquals("Online", BookingChannels.from(configuration).name());
    }

    /** Negative scenario. */
    @Test
    void unknownChannelIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> BookingChannels.byName("telegram"));
    }
}
