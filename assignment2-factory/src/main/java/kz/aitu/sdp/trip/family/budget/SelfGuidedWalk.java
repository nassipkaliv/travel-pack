package kz.aitu.sdp.trip.family.budget;

import java.util.List;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.TripTier;

class SelfGuidedWalk implements Excursion {

    @Override
    public TripTier tier() {
        return TripTier.BUDGET;
    }

    @Override
    public String describe() {
        return "Self-guided city walk with a paper map";
    }

    @Override
    public String guide() {
        return "none";
    }

    @Override
    public int groupSize() {
        return 6;
    }

    @Override
    public int durationMinutes() {
        return 180;
    }

    @Override
    public int startHour() {
        return 10;
    }

    @Override
    public boolean flexibleStart() {
        return true;
    }

    @Override
    public int pricePerPerson() {
        return 0;
    }

    @Override
    public List<String> packingItems() {
        return List.of("walking shoes", "water bottle");
    }
}
