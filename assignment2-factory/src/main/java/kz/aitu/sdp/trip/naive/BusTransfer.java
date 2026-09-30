package kz.aitu.sdp.trip.naive;

/** BUDGET transport: cheap, slow, small baggage. */
public class BusTransfer implements Transport {

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
    public int pricePerPerson() {
        return 8_000;
    }
}
