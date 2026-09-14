package kz.aitu.sdp.travel;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Interactive console: the user types the values, the Builder creates the package. */
public final class TravelConsole {

    private final ConsoleInput input;
    private final PrintStream out;

    public TravelConsole(Scanner scanner, PrintStream out) {
        this.input = new ConsoleInput(scanner, out);
        this.out = out;
    }

    public void run() {
        out.println("=== Travel Package Builder ===");
        try {
            while (true) {
                printMenu();
                switch (input.text("Choose")) {
                    case "1" -> createPackage(false);
                    case "2" -> createPackage(true);
                    case "0" -> {
                        out.println("Bye!");
                        return;
                    }
                    default -> out.println("Choose 1, 2 or 0.");
                }
            }
        } catch (NoSuchElementException endOfInput) {
            out.println();
        }
    }

    private void printMenu() {
        out.println();
        out.println("1) Build a package from scratch");
        out.println("2) Start from preset allInclusiveBeach");
        out.println("0) Exit");
    }

    private void createPackage(boolean fromPreset) {
        out.println();
        out.println(fromPreset
                ? "Preset sets: 7 nights, PREMIUM budget, ALL_INCLUSIVE meals, airport transfer."
                : "Defaults: STANDARD budget, STANDARD room, BREAKFAST, 1 adult, 0 children.");
        try {
            TravelPackage.Builder builder = startBuilder(fromPreset);
            askDetails(builder, fromPreset);
            printCreated(builder.build());
        } catch (IllegalArgumentException e) {
            printRejectedImmediately(e);
        } catch (InvalidTravelPackageException e) {
            printInvalid(e);
        }
    }

    private TravelPackage.Builder startBuilder(boolean fromPreset) {
        String id = input.text("Package id");
        Location from = input.location("From");
        Location to = input.location("To");
        return fromPreset
                ? TravelPresets.allInclusiveBeach(id, from, to)
                : TravelPackage.builder(id, from, to);
    }

    private void askDetails(TravelPackage.Builder builder, boolean fromPreset) {
        input.optionalNumber("Nights (1..30)", fromPreset ? "keep 7" : "skip").ifPresent(builder::nights);
        input.optionalChoice("Budget", BudgetLevel.values(),
                budget -> "hotel %d-%d stars".formatted(budget.minStars(), budget.maxStars()))
                .ifPresent(builder::budget);
        askHotel(builder);
        input.optionalChoice("Room", RoomType.values(), room -> "fits " + room.capacity())
                .ifPresent(builder::room);
        input.optionalChoice("Meals", MealPlan.values(), meals -> "").ifPresent(builder::meals);
        askTravelers(builder);
        input.optionalText("Flight number", "no flight").ifPresent(builder::flight);
        input.optionalText("Train number", "no train").ifPresent(builder::train);
        askExtras(builder, fromPreset);
    }

    private void askHotel(TravelPackage.Builder builder) {
        input.optionalText("Hotel name", "no hotel")
                .ifPresent(name -> builder.hotel(name, input.number("Hotel stars (1..5)")));
    }

    private void askTravelers(TravelPackage.Builder builder) {
        int adults = input.optionalNumber("Adults", "1").orElse(1);
        int children = input.optionalNumber("Children", "0").orElse(0);
        builder.travelers(adults, children);
    }

    private void askExtras(TravelPackage.Builder builder, boolean fromPreset) {
        if (fromPreset) {
            out.println("Airport transfer: already included by the preset");
        } else if (input.yesNo("Airport transfer")) {
            builder.airportTransfer();
        }
        if (input.yesNo("Visa support")) {
            builder.visaSupport();
        }
    }

    private void printCreated(TravelPackage trip) {
        out.println();
        out.println("✔ Package created");
        out.println("  Id:         " + trip.id());
        out.println("  Route:      %s, %s -> %s, %s%s".formatted(
                trip.from().city(), trip.from().country(), trip.to().city(), trip.to().country(),
                trip.isInternational() ? " (international)" : ""));
        out.println("  Nights:     " + trip.nights());
        out.println("  Budget:     " + trip.budget());
        out.println("  Hotel:      %s, %d stars".formatted(trip.hotel().name(), trip.hotel().stars()));
        out.println("  Room:       " + trip.room());
        out.println("  Meals:      " + trip.meals());
        out.println("  Travelers:  %d adults, %d children".formatted(trip.adults(), trip.children()));
        out.println("  Transport:  " + transport(trip));
        out.println("  Transfer:   " + (trip.hasAirportTransfer() ? "yes" : "no"));
        out.println("  Visa:       " + (trip.hasVisaSupport() ? "yes" : "no"));
    }

    private static String transport(TravelPackage trip) {
        return trip.flight()
                .map(flight -> "flight " + flight.number())
                .orElseGet(() -> "train " + trip.train().orElseThrow().number());
    }

    private void printRejectedImmediately(IllegalArgumentException e) {
        out.println();
        out.println("✘ Rejected right away: " + e.getMessage());
    }

    private void printInvalid(InvalidTravelPackageException e) {
        out.println();
        out.println("✘ Package is invalid (" + e.errors().size() + " errors):");
        e.errors().forEach(error -> out.println("  - " + error));
    }
}
