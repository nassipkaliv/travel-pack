package kz.aitu.sdp.trip.naive;

/** STANDARD accommodation: private room with breakfast. */
public class Hotel3Star implements Accommodation {

    @Override
    public String describe() {
        return "3-star hotel, double room";
    }

    @Override
    public boolean hasPrivateRoom() {
        return true;
    }

    @Override
    public String mealsIncluded() {
        return "breakfast";
    }

    @Override
    public int pricePerNight() {
        return 25_000;
    }
}
