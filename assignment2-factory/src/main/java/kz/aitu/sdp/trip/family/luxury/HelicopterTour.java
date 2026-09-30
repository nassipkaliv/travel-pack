package kz.aitu.sdp.trip.family.luxury;

import java.util.List;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.TripTier;

class HelicopterTour implements Excursion {

    @Override
    public TripTier tier() {
        return TripTier.LUXURY;
    }

    @Override
    public String describe() {
        return "Helicopter tour over the coast";
    }

    @Override
    public String guide() {
        return "personal pilot-guide";
    }

    @Override
    public int groupSize() {
        return 4;
    }

    @Override
    public int durationMinutes() {
        return 120;
    }

    @Override
    public int startHour() {
        return 9;
    }

    @Override
    public boolean flexibleStart() {
        return true;
    }

    @Override
    public int pricePerPerson() {
        return 300_000;
    }

    @Override
    public List<String> packingItems() {
        return List.of("camera", "light jacket");
    }
}
