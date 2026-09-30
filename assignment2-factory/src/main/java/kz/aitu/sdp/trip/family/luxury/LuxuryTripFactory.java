package kz.aitu.sdp.trip.family.luxury;

import kz.aitu.sdp.trip.factory.TripFactory;
import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

/** Fourth family, added without touching the business logic (Part G). */
public final class LuxuryTripFactory implements TripFactory {

    @Override
    public TripTier tier() {
        return TripTier.LUXURY;
    }

    @Override
    public Transport createTransport() {
        return new PrivateJet();
    }

    @Override
    public Accommodation createAccommodation() {
        return new PrivateVilla();
    }

    @Override
    public Excursion createExcursion() {
        return new HelicopterTour();
    }
}
