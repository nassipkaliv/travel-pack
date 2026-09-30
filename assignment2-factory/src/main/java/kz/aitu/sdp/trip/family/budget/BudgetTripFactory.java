package kz.aitu.sdp.trip.family.budget;

import kz.aitu.sdp.trip.factory.TripFactory;
import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

/** Concrete factory of the BUDGET family: bus, hostel, self-guided walk. */
public final class BudgetTripFactory implements TripFactory {

    @Override
    public TripTier tier() {
        return TripTier.BUDGET;
    }

    @Override
    public Transport createTransport() {
        return new BusTransfer();
    }

    @Override
    public Accommodation createAccommodation() {
        return new Hostel();
    }

    @Override
    public Excursion createExcursion() {
        return new SelfGuidedWalk();
    }
}
