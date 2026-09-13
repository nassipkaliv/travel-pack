package kz.aitu.sdp.travel;

public final class App {

    public static void main(String[] args) {
        Location almaty = new Location("Almaty", "Kazakhstan");
        Location astana = new Location("Astana", "Kazakhstan");
        Location antalya = new Location("Antalya", "Turkey");

        TravelPackage beach = TravelPresets.allInclusiveBeach("BEACH-1", almaty, antalya)
                .hotel("Rixos", 5)
                .flight("KC 921")
                .travelers(2, 0)
                .visaSupport()
                .build();
        System.out.println("Preset: " + beach);

        TravelPackage family = TravelPackage.builder("FAMILY-1", almaty, astana)
                .nights(4)
                .hotel("Hilton", 4)
                .room(RoomType.FAMILY)
                .travelers(2, 2)
                .train("002")
                .build();
        System.out.println("Custom: " + family);

        try {
            TravelPackage.builder("BAD-1", almaty, antalya)
                    .nights(45)
                    .budget(BudgetLevel.ECONOMY)
                    .hotel("Palace", 5)
                    .travelers(2, 2)
                    .train("002")
                    .airportTransfer()
                    .build();
        } catch (InvalidTravelPackageException e) {
            System.out.println("Rejected:");
            e.errors().forEach(error -> System.out.println("  - " + error));
        }
    }
}
