package kz.aitu.sdp.trip.family.premium;

import java.util.List;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.TripTier;

class PrivateGuide implements Excursion {

    @Override
    public TripTier tier() {
        return TripTier.PREMIUM;
    }

    @Override
    public String describe() {
        return "Private guide with a car, flexible schedule";
    }

    @Override
    public String guide() {
        return "personal guide";
    }

    @Override
    public int groupSize() {
        return 3;
    }

    @Override
    public int durationMinutes() {
        return 300;
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
        return 90_000;
    }

    @Override
    public List<String> packingItems() {
        return List.of("sunglasses");
    }
}
