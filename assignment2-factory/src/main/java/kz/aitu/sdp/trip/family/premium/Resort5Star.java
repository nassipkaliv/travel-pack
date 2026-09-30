package kz.aitu.sdp.trip.family.premium;

import java.util.List;
import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.MealPlan;
import kz.aitu.sdp.trip.product.TripTier;

class Resort5Star implements Accommodation {

    @Override
    public TripTier tier() {
        return TripTier.PREMIUM;
    }

    @Override
    public String describe() {
        return "5-star resort, sea view suite";
    }

    @Override
    public boolean privateRoom() {
        return true;
    }

    @Override
    public MealPlan meals() {
        return MealPlan.ALL_INCLUSIVE;
    }

    @Override
    public int checkInHour() {
        return 12;
    }

    @Override
    public int pricePerNight() {
        return 120_000;
    }

    @Override
    public List<String> packingItems() {
        return List.of("swimwear", "spa slippers");
    }
}
