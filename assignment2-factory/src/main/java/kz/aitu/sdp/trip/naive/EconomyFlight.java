package kz.aitu.sdp.trip.naive;

/** STANDARD transport: regular flight, checked baggage. */
public class EconomyFlight implements Transport {

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
    public int pricePerPerson() {
        return 60_000;
    }
}
