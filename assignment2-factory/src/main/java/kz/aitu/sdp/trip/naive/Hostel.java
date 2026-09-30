package kz.aitu.sdp.trip.naive;

/** BUDGET accommodation: shared room, no meals. */
public class Hostel implements Accommodation {

    @Override
    public String describe() {
        return "Hostel, 6-bed shared room";
    }

    @Override
    public boolean hasPrivateRoom() {
        return false;
    }

    @Override
    public String mealsIncluded() {
        return "none";
    }

    @Override
    public int pricePerNight() {
        return 5_000;
    }
}
