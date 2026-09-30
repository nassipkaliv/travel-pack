package kz.aitu.sdp.trip.naive;

/** PREMIUM excursion: personal guide and a car. */
public class PrivateGuide implements Excursion {

    @Override
    public String describe() {
        return "Private guide with a car, flexible schedule";
    }

    @Override
    public String guide() {
        return "personal guide";
    }

    @Override
    public int groupSize() {
        return 2;
    }

    @Override
    public int pricePerPerson() {
        return 90_000;
    }
}
