package kz.aitu.sdp.trip.family.standard;

import kz.aitu.sdp.trip.factory.TripFactory;
import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

/** Concrete factory of the STANDARD family: economy flight, 3-star hotel, group tour. */
public final class StandardTripFactory implements TripFactory {

    @Override
    public TripTier tier() {
        return TripTier.STANDARD;
    }

    @Override
    public Transport createTransport() {
        return new EconomyFlight();
    }

    @Override
    public Accommodation createAccommodation() {
        return new Hotel3Star();
    }

    @Override
    public Excursion createExcursion() {
        return new GroupTour();
    }
}
