package kz.aitu.sdp.trip.family.standard;

import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

class EconomyFlight implements Transport {

    @Override
    public TripTier tier() {
        return TripTier.STANDARD;
    }

    @Override
    public String describe() {
        return "Economy flight, checked baggage included";
    }

    @Override
    public int travelTimeMinutes() {
        return 2 * 60;
    }

    @Override
    public int baggageKg() {
        return 20;
    }

    @Override
    public boolean doorToDoor() {
        return false;
    }

    @Override
    public int pricePerPerson() {
        return 60_000;
    }
}
