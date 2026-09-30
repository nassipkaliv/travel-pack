package kz.aitu.sdp.trip.naive;

/** PREMIUM transport: business class, lounge, large baggage. */
public class BusinessFlight implements Transport {

    @Override
    public String describe() {
        return "Business class flight, lounge access";
    }

    @Override
    public int travelTimeMinutes() {
        return 2 * 60;
    }

    @Override
    public int baggageKg() {
        return 40;
    }

    @Override
    public int pricePerPerson() {
        return 220_000;
    }
}
