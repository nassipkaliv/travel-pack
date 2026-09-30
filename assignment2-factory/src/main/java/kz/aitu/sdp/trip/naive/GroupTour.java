package kz.aitu.sdp.trip.naive;

/** STANDARD excursion: shared guide, fixed schedule. */
public class GroupTour implements Excursion {

    @Override
    public String describe() {
        return "Bus city tour with a shared guide";
    }

    @Override
    public String guide() {
        return "shared group guide";
    }

    @Override
    public int groupSize() {
        return 30;
    }

    @Override
    public int pricePerPerson() {
        return 12_000;
    }
}
