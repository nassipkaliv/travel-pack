package kz.aitu.sdp.travel;

import java.util.Objects;
import java.util.Optional;

/** Immutable travel package. Created only through {@link Builder}, so it is always valid. */
public final class TravelPackage {

    private final String id;
    private final Location from;
    private final Location to;
    private final int nights;
    private final BudgetLevel budget;
    private final Hotel hotel;
    private final RoomType room;
    private final MealPlan meals;
    private final int adults;
    private final int children;
    private final Flight flight;
    private final Train train;
    private final boolean airportTransfer;
    private final boolean visaSupport;

    private TravelPackage(Builder builder) {
        id = builder.id;
        from = builder.from;
        to = builder.to;
        nights = builder.nights;
        budget = builder.budget;
        hotel = builder.hotel;
        room = builder.room;
        meals = builder.meals;
        adults = builder.adults;
        children = builder.children;
        flight = builder.flight;
        train = builder.train;
        airportTransfer = builder.airportTransfer;
        visaSupport = builder.visaSupport;
    }

    public static Builder builder(String id, Location from, Location to) {
        return new Builder(id, from, to);
    }

    public String id() { return id; }
    public Location from() { return from; }
    public Location to() { return to; }
    public int nights() { return nights; }
    public BudgetLevel budget() { return budget; }
    public Hotel hotel() { return hotel; }
    public RoomType room() { return room; }
    public MealPlan meals() { return meals; }
    public int adults() { return adults; }
    public int children() { return children; }
    public int guests() { return adults + children; }
    public Optional<Flight> flight() { return Optional.ofNullable(flight); }
    public Optional<Train> train() { return Optional.ofNullable(train); }
    public boolean hasAirportTransfer() { return airportTransfer; }
    public boolean hasVisaSupport() { return visaSupport; }

    public boolean isInternational() {
        return !from.country().equalsIgnoreCase(to.country());
    }

    @Override
    public String toString() {
        return "%s: %s -> %s, %d nights, %s, %s, %s room, %s, guests %d+%d, %s%s%s".formatted(
                id, from.city(), to.city(), nights, budget, hotel, room, meals, adults, children,
                flight != null ? flight : train,
                airportTransfer ? ", transfer" : "",
                visaSupport ? ", visa" : "");
    }

    /** Collects values in any order; all cross-field rules are checked in {@link #build()}. */
    public static final class Builder {

        private final String id;
        private final Location from;
        private final Location to;
        private int nights;
        private BudgetLevel budget = BudgetLevel.STANDARD;
        private Hotel hotel;
        private RoomType room = RoomType.STANDARD;
        private MealPlan meals = MealPlan.BREAKFAST;
        private int adults = 1;
        private int children;
        private Flight flight;
        private Train train;
        private boolean airportTransfer;
        private boolean visaSupport;

        private Builder(String id, Location from, Location to) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            this.id = id;
            this.from = Objects.requireNonNull(from, "from");
            this.to = Objects.requireNonNull(to, "to");
        }

        public Builder nights(int nights) {
            this.nights = nights;
            return this;
        }

        public Builder budget(BudgetLevel budget) {
            this.budget = Objects.requireNonNull(budget, "budget");
            return this;
        }

        public Builder hotel(String name, int stars) {
            this.hotel = new Hotel(name, stars);
            return this;
        }

        public Builder room(RoomType room) {
            this.room = Objects.requireNonNull(room, "room");
            return this;
        }

        public Builder meals(MealPlan meals) {
            this.meals = Objects.requireNonNull(meals, "meals");
            return this;
        }

        public Builder travelers(int adults, int children) {
            if (adults < 1 || children < 0) {
                throw new IllegalArgumentException("Need at least 1 adult and 0+ children");
            }
            this.adults = adults;
            this.children = children;
            return this;
        }

        public Builder flight(String number) {
            this.flight = new Flight(number);
            return this;
        }

        public Builder train(String number) {
            this.train = new Train(number);
            return this;
        }

        public Builder airportTransfer() {
            this.airportTransfer = true;
            return this;
        }

        public Builder visaSupport() {
            this.visaSupport = true;
            return this;
        }

        /** @throws InvalidTravelPackageException with every broken rule */
        public TravelPackage build() {
            TravelPackage result = new TravelPackage(this);
            new TravelPackageValidator(result).validate();
            return result;
        }
    }
}
