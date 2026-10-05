package kz.aitu.sdp.trip.factory;

import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

/**
 * Abstract Factory: creates one consistent family of trip components.
 *
 */
public interface TripFactory {

    TripTier tier();

    Transport createTransport();

    Accommodation createAccommodation();

    Excursion createExcursion();
}
