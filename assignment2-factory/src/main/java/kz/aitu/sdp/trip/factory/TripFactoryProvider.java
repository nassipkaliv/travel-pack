package kz.aitu.sdp.trip.factory;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import kz.aitu.sdp.trip.config.ExternalConfiguration;
import kz.aitu.sdp.trip.family.budget.BudgetTripFactory;
import kz.aitu.sdp.trip.family.luxury.LuxuryTripFactory;
import kz.aitu.sdp.trip.family.premium.PremiumTripFactory;
import kz.aitu.sdp.trip.family.standard.StandardTripFactory;
import kz.aitu.sdp.trip.product.TripTier;

/**
 * Runtime selection of the product family (Part E).
 *
 * <p>The client never writes {@code new PremiumTripFactory()}: it asks the provider, which
 * reads the family from {@code --family=...}, the {@code TRIP_FAMILY} variable or
 * {@code trip.properties}. This registry is the only place that names concrete factories.
 */
public final class TripFactoryProvider {

    public static final String OPTION = "family";
    public static final String ENVIRONMENT_VARIABLE = "TRIP_FAMILY";
    public static final TripTier DEFAULT_TIER = TripTier.STANDARD;

    private static final Map<TripTier, Supplier<TripFactory>> FACTORIES = new EnumMap<>(TripTier.class);

    static {
        FACTORIES.put(TripTier.BUDGET, BudgetTripFactory::new);
        FACTORIES.put(TripTier.STANDARD, StandardTripFactory::new);
        FACTORIES.put(TripTier.PREMIUM, PremiumTripFactory::new);
        FACTORIES.put(TripTier.LUXURY, LuxuryTripFactory::new);
    }

    private TripFactoryProvider() {
    }

    public static Set<TripTier> supportedTiers() {
        return Set.copyOf(FACTORIES.keySet());
    }

    public static TripFactory forTier(TripTier tier) {
        Supplier<TripFactory> factory = FACTORIES.get(tier);
        if (factory == null) {
            throw new IllegalArgumentException("No factory registered for " + tier);
        }
        return factory.get();
    }

    public static TripFactory from(ExternalConfiguration configuration) {
        return forTier(resolveTier(configuration));
    }

    public static TripFactory fromRuntime(String[] arguments) {
        return from(ExternalConfiguration.fromRuntime(arguments));
    }

    public static TripTier resolveTier(ExternalConfiguration configuration) {
        return configuration.value(OPTION, ENVIRONMENT_VARIABLE, OPTION)
                .map(TripFactoryProvider::parseTier)
                .orElse(DEFAULT_TIER);
    }

    private static TripTier parseTier(String value) {
        try {
            return TripTier.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown product family '%s'. Known families: %s".formatted(value, FACTORIES.keySet()));
        }
    }
}
