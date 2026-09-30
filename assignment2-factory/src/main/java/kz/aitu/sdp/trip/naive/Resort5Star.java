package kz.aitu.sdp.trip.naive;

/** PREMIUM accommodation: suite, all inclusive. */
public class Resort5Star implements Accommodation {

    @Override
    public String describe() {
        return "5-star resort, sea view suite";
    }

    @Override
    public boolean hasPrivateRoom() {
        return true;
    }

    @Override
    public String mealsIncluded() {
        return "all inclusive";
    }

    @Override
    public int pricePerNight() {
        return 120_000;
    }
}
