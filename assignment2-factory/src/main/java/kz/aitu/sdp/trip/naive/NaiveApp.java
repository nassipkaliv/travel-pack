package kz.aitu.sdp.trip.naive;

/** Demo of the version without factories. */
public final class NaiveApp {

    public static void main(String[] args) {
        TripBookingService service = new TripBookingService();

        System.out.println(service.bookTrip("BUDGET", 3, 2));
        System.out.println();
        System.out.println(service.bookTrip("PREMIUM", 7, 2));
        System.out.println();

        System.out.println("Nobody prevents this combination:");
        System.out.println(service.bookMixedTrip(3, 1));
    }
}
