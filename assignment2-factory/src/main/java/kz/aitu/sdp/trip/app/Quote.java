package kz.aitu.sdp.trip.app;

/** Price breakdown of a trip, in KZT. */
public record Quote(int transportCost, int accommodationCost, int excursionCost, int travelers) {

    public int total() {
        return transportCost + accommodationCost + excursionCost;
    }

    public int perPerson() {
        return total() / travelers;
    }

    public boolean fitsBudget(int maxPerPerson) {
        return perPerson() <= maxPerPerson;
    }
}
