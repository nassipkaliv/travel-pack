package kz.aitu.sdp.trip.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import kz.aitu.sdp.trip.family.budget.BudgetTripFactory;
import kz.aitu.sdp.trip.family.premium.PremiumTripFactory;
import kz.aitu.sdp.trip.family.standard.StandardTripFactory;
import kz.aitu.sdp.trip.product.TripTier;

/**
 * Runtime selection of the product family (Part E).
 *
 * <p>The client never writes {@code new PremiumTripFactory()}. It asks the provider, which
 * resolves the family from an external source, in this order:
 * command line argument {@code --family=...}, environment variable {@code TRIP_FAMILY},
 * {@code trip.properties} on the classpath, and finally the default family.
 *
 * <p>This registry is the single place that knows the concrete factory classes.
 */
public final class TripFactoryProvider {

    public static final String ARGUMENT = "--family=";
    public static final String ENVIRONMENT_VARIABLE = "TRIP_FAMILY";
    public static final String PROPERTY = "family";
    public static final TripTier DEFAULT_TIER = TripTier.STANDARD;

    private static final Map<TripTier, Supplier<TripFactory>> FACTORIES = new EnumMap<>(TripTier.class);

    static {
        FACTORIES.put(TripTier.BUDGET, BudgetTripFactory::new);
        FACTORIES.put(TripTier.STANDARD, StandardTripFactory::new);
        FACTORIES.put(TripTier.PREMIUM, PremiumTripFactory::new);
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

    /** Resolves the family the way the running application does. */
    public static TripFactory fromRuntime(String[] args) {
        return fromSources(args, System.getenv(ENVIRONMENT_VARIABLE), loadProperties());
    }

    /** Same resolution, with every external source passed in, so it can be tested. */
    public static TripFactory fromSources(String[] args, String environmentValue, Properties properties) {
        return forTier(resolveTier(args, environmentValue, properties));
    }

    public static TripTier resolveTier(String[] args, String environmentValue, Properties properties) {
        return argumentValue(args)
                .or(() -> Optional.ofNullable(environmentValue))
                .or(() -> Optional.ofNullable(properties.getProperty(PROPERTY)))
                .map(TripFactoryProvider::parseTier)
                .orElse(DEFAULT_TIER);
    }

    private static Optional<String> argumentValue(String[] args) {
        if (args == null) {
            return Optional.empty();
        }
        for (String argument : args) {
            if (argument.startsWith(ARGUMENT)) {
                return Optional.of(argument.substring(ARGUMENT.length()));
            }
        }
        return Optional.empty();
    }

    private static TripTier parseTier(String value) {
        try {
            return TripTier.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown product family '%s'. Known families: %s".formatted(value, FACTORIES.keySet()));
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = TripFactoryProvider.class.getResourceAsStream("/trip.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read trip.properties", e);
        }
        return properties;
    }
}
