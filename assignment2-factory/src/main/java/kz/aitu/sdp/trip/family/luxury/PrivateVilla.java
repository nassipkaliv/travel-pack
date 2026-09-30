package kz.aitu.sdp.trip.family.luxury;

import java.util.List;
import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.MealPlan;
import kz.aitu.sdp.trip.product.TripTier;

class PrivateVilla implements Accommodation {

    @Override
    public TripTier tier() {
        return TripTier.LUXURY;
    }

    @Override
    public String describe() {
        return "Private villa with a chef and a butler";
    }

    @Override
    public boolean privateRoom() {
        return true;
    }

    @Override
    public MealPlan meals() {
        return MealPlan.ALL_INCLUSIVE;
    }

    /** The villa is rented for the whole day, so the guests arrive whenever they want. */
    @Override
    public int checkInHour() {
        return 0;
    }

    @Override
    public int pricePerNight() {
        return 450_000;
    }

    @Override
    public List<String> packingItems() {
        return List.of("evening outfit");
    }
}
