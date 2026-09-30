package kz.aitu.sdp.trip.naive;

/** BUDGET excursion: a map and a route, no guide. */
public class SelfGuidedWalk implements Excursion {

    @Override
    public String describe() {
        return "Self-guided city walk with a paper map";
    }

    @Override
    public String guide() {
        return "none";
    }

    @Override
    public int groupSize() {
        return 1;
    }

    @Override
    public int pricePerPerson() {
        return 0;
    }
}
