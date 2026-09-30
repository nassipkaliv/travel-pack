package kz.aitu.sdp.trip.factory;

import kz.aitu.sdp.trip.product.Accommodation;
import kz.aitu.sdp.trip.product.Excursion;
import kz.aitu.sdp.trip.product.Transport;
import kz.aitu.sdp.trip.product.TripTier;

/**
 * Abstract Factory: creates one consistent family of trip components.
 *
 * <p>A factory instance is the only source of components, and every component it returns
 * belongs to {@link #tier()}. Mixing families is therefore not a rule that has to be
 * checked — there is no way for the client to get components of two tiers from one factory.
 */
public interface TripFactory {

    TripTier tier();

    Transport createTransport();

    Accommodation createAccommodation();

    Excursion createExcursion();
}
