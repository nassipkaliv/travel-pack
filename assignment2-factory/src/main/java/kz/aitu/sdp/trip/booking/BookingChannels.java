package kz.aitu.sdp.trip.booking;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import kz.aitu.sdp.trip.config.ExternalConfiguration;

/** Runtime selection of the booking channel, the same way the family is selected. */
public final class BookingChannels {

    public static final String OPTION = "channel";
    public static final String ENVIRONMENT_VARIABLE = "TRIP_CHANNEL";
    public static final String DEFAULT_CHANNEL = "online";

    private static final Map<String, Supplier<BookingChannel>> CHANNELS = new LinkedHashMap<>();

    static {
        CHANNELS.put("online", OnlineBookingChannel::new);
        CHANNELS.put("agency", AgencyBookingChannel::new);
        CHANNELS.put("corporate", CorporateBookingChannel::new);
    }

    private BookingChannels() {
    }

    public static Set<String> supportedChannels() {
        return Set.copyOf(CHANNELS.keySet());
    }

    public static BookingChannel byName(String name) {
        Supplier<BookingChannel> channel = CHANNELS.get(name.toLowerCase());
        if (channel == null) {
            throw new IllegalArgumentException(
                    "Unknown booking channel '%s'. Known channels: %s".formatted(name, CHANNELS.keySet()));
        }
        return channel.get();
    }

    public static BookingChannel from(ExternalConfiguration configuration) {
        return byName(configuration.value(OPTION, ENVIRONMENT_VARIABLE, OPTION).orElse(DEFAULT_CHANNEL));
    }
}
