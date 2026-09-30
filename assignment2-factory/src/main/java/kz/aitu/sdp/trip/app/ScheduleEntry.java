package kz.aitu.sdp.trip.app;

/** One line of the itinerary. {@code minutes} counts from 00:00 of the departure day. */
public record ScheduleEntry(int minutes, String activity) {

    private static final int MINUTES_PER_DAY = 24 * 60;

    public int day() {
        return minutes / MINUTES_PER_DAY + 1;
    }

    public String time() {
        int minuteOfDay = minutes % MINUTES_PER_DAY;
        return "%02d:%02d".formatted(minuteOfDay / 60, minuteOfDay % 60);
    }

    @Override
    public String toString() {
        return "Day %d %s  %s".formatted(day(), time(), activity);
    }
}
