package kz.aitu.sdp.trip.family.premium;

import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

class BusinessFlight implements Transport {

    @Override
    public TripTier tier() {
        return TripTier.PREMIUM;
    }

    @Override
    public String describe() {
        return "Business class flight, lounge access";
    }

    @Override
    public int travelTimeMinutes() {
        return 2 * 60;
    }

    @Override
    public int baggageKg() {
        return 40;
    }

    @Override
    public boolean doorToDoor() {
        return false;
    }

    @Override
    public int pricePerPerson() {
        return 220_000;
    }
}
