package kz.aitu.sdp.trip.family.standard;

import java.util.List;
import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.MealPlan;
import kz.aitu.sdp.trip.product.TripTier;

class Hotel3Star implements Accommodation {

    @Override
    public TripTier tier() {
        return TripTier.STANDARD;
    }

    @Override
    public String describe() {
        return "3-star hotel, double room";
    }

    @Override
    public boolean privateRoom() {
        return true;
    }

    @Override
    public MealPlan meals() {
        return MealPlan.BREAKFAST;
    }

    @Override
    public int checkInHour() {
        return 14;
    }

    @Override
    public int pricePerNight() {
        return 25_000;
    }

    @Override
    public List<String> packingItems() {
        return List.of("power adapter");
    }
}
