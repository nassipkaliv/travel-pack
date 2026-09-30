package kz.aitu.sdp.trip.product;

import java.util.List;

/** Product type 2: where the traveler stays. */
public interface Accommodation extends TripComponent {

    boolean privateRoom();

    MealPlan meals();

    /** Earliest check-in hour, 0 means "any time". Shifts the first day schedule. */
    int checkInHour();

    int pricePerNight();

    /** What this kind of stay forces the traveler to bring. */
    List<String> packingItems();
}
