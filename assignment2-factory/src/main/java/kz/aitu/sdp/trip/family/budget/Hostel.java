package kz.aitu.sdp.trip.family.budget;

import java.util.List;
import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.MealPlan;
import kz.aitu.sdp.trip.product.TripTier;

class Hostel implements Accommodation {

    @Override
    public TripTier tier() {
        return TripTier.BUDGET;
    }

    @Override
    public String describe() {
        return "Hostel, 6-bed shared room";
    }

    @Override
    public boolean privateRoom() {
        return false;
    }

    @Override
    public MealPlan meals() {
        return MealPlan.NONE;
    }

    @Override
    public int checkInHour() {
        return 16;
    }

    @Override
    public int pricePerNight() {
        return 5_000;
    }

    @Override
    public List<String> packingItems() {
        return List.of("towel", "padlock", "earplugs");
    }
}
