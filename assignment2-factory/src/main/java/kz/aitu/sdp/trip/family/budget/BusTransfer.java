package kz.aitu.sdp.trip.family.budget;

import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

/** Package-private: only {@link BudgetTripFactory} can create it. */
class BusTransfer implements Transport {

    @Override
    public TripTier tier() {
        return TripTier.BUDGET;
    }

    @Override
    public String describe() {
        return "Intercity bus, seat only";
    }

    @Override
    public int travelTimeMinutes() {
        return 14 * 60;
    }

    @Override
    public int baggageKg() {
        return 10;
    }

    @Override
    public boolean doorToDoor() {
        return false;
    }

    @Override
    public int pricePerPerson() {
        return 8_000;
    }
}
