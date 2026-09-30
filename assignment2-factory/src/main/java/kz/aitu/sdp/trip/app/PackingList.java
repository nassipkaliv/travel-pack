package kz.aitu.sdp.trip.app;

import java.util.List;

/** What to pack, derived from all three products together. */
public record PackingList(int baggageLimitKg, List<String> items, List<String> advice) {

    public PackingList {
        items = List.copyOf(items);
        advice = List.copyOf(advice);
    }
}
