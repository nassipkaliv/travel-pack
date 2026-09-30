package kz.aitu.sdp.trip.family.standard;

import java.util.List;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.TripTier;

class GroupTour implements Excursion {

    @Override
    public TripTier tier() {
        return TripTier.STANDARD;
    }

    @Override
    public String describe() {
        return "Bus city tour with a shared guide";
    }

    @Override
    public String guide() {
        return "shared group guide";
    }

    @Override
    public int groupSize() {
        return 30;
    }

    @Override
    public int durationMinutes() {
        return 240;
    }

    @Override
    public int startHour() {
        return 9;
    }

    @Override
    public boolean flexibleStart() {
        return false;
    }

    @Override
    public int pricePerPerson() {
        return 12_000;
    }

    @Override
    public List<String> packingItems() {
        return List.of("comfortable shoes", "camera");
    }
}
