package kz.aitu.sdp.trip.family.premium;

import kz.aitu.sdp.trip.factory.TripFactory;
import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

/** Concrete factory of the PREMIUM family: business flight, resort, private guide. */
public final class PremiumTripFactory implements TripFactory {

    @Override
    public TripTier tier() {
        return TripTier.PREMIUM;
    }

    @Override
    public Transport createTransport() {
        return new BusinessFlight();
    }

    @Override
    public Accommodation createAccommodation() {
        return new Resort5Star();
    }

    @Override
    public Excursion createExcursion() {
        return new PrivateGuide();
    }
}
