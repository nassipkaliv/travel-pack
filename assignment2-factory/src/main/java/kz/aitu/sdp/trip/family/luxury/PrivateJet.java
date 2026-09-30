package kz.aitu.sdp.trip.family.luxury;

import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

class PrivateJet implements Transport {

    @Override
    public TripTier tier() {
        return TripTier.LUXURY;
    }

    @Override
    public String describe() {
        return "Private jet, direct flight";
    }

    @Override
    public int travelTimeMinutes() {
        return 90;
    }

    @Override
    public int baggageKg() {
        return 100;
    }

    @Override
    public boolean doorToDoor() {
        return true;
    }

    @Override
    public int pricePerPerson() {
        return 900_000;
    }
}
