package kz.aitu.sdp.trip.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.UnaryOperator;

/**
 * External sources the application reads its settings from (Part E).
 *
 * <p>Lookup order: command line argument, environment variable, properties file.
 * Tests build an instance with their own sources instead of touching the real environment.
 */
public final class ExternalConfiguration {

    public static final String PROPERTIES_FILE = "/trip.properties";

    private final String[] arguments;
    private final UnaryOperator<String> environment;
    private final Properties properties;

    public ExternalConfiguration(String[] arguments, UnaryOperator<String> environment, Properties properties) {
        this.arguments = arguments == null ? new String[0] : arguments.clone();
        this.environment = Objects.requireNonNull(environment, "environment");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    /** Real sources: the command line, the environment and trip.properties on the classpath. */
    public static ExternalConfiguration fromRuntime(String[] arguments) {
        return new ExternalConfiguration(arguments, System::getenv, loadProperties());
    }

    public Optional<String> value(String option, String environmentVariable, String propertyKey) {
        return argument(option)
                .or(() -> Optional.ofNullable(environment.apply(environmentVariable)))
                .or(() -> Optional.ofNullable(properties.getProperty(propertyKey)))
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }

    private Optional<String> argument(String option) {
        String prefix = "--" + option + "=";
        for (String argument : arguments) {
            if (argument.startsWith(prefix)) {
                return Optional.of(argument.substring(prefix.length()));
            }
        }
        return Optional.empty();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = ExternalConfiguration.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + PROPERTIES_FILE, e);
        }
        return properties;
    }
}
