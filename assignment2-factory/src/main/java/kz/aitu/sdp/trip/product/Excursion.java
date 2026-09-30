package kz.aitu.sdp.trip.product;

import java.util.List;

/** Product type 3: what the traveler does at the destination. */
public interface Excursion extends TripComponent {

    String guide();

    /** Maximum number of participants this excursion can host. */
    int groupSize();

    int durationMinutes();

    /** Fixed departure hour of the excursion; ignored when {@link #flexibleStart()} is true. */
    int startHour();

    /** True when the excursion starts whenever the traveler is ready. */
    boolean flexibleStart();

    int pricePerPerson();

    List<String> packingItems();
}
