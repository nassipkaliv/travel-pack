package kz.aitu.sdp.travel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TravelPackageTest {

    static final Location ALMATY = new Location("Almaty", "Kazakhstan");
    static final Location ASTANA = new Location("Astana", "Kazakhstan");
    static final Location ANTALYA = new Location("Antalya", "Turkey");

    /** Valid package. Each test breaks exactly one thing. */
    static TravelPackage.Builder valid() {
        return TravelPackage.builder("T", ALMATY, ASTANA).nights(3).hotel("Hilton", 4).train("002");
    }

    static void assertError(TravelPackage.Builder builder, String text) {
        var e = assertThrows(InvalidTravelPackageException.class, builder::build);
        assertTrue(e.errors().stream().anyMatch(msg -> msg.contains(text)), e.errors().toString());
    }

    // ---- Builder ----

    @Test
    void buildsWithDefaults() {
        TravelPackage p = valid().build();

        assertEquals(BudgetLevel.STANDARD, p.budget());
        assertEquals(RoomType.STANDARD, p.room());
        assertEquals(MealPlan.BREAKFAST, p.meals());
        assertEquals(1, p.adults());
        assertFalse(p.isInternational());
    }

    @Test
    void buildsWithAllValues() {
        TravelPackage p = TravelPackage.builder("T", ALMATY, ANTALYA)
                .nights(10).budget(BudgetLevel.PREMIUM).hotel("Rixos", 5)
                .room(RoomType.FAMILY).meals(MealPlan.ALL_INCLUSIVE).travelers(2, 1)
                .flight("KC 921").airportTransfer().visaSupport()
                .build();

        assertEquals(10, p.nights());
        assertEquals(new Hotel("Rixos", 5), p.hotel());
        assertEquals(3, p.guests());
        assertEquals("KC 921", p.flight().orElseThrow().number());
        assertTrue(p.isInternational());
        assertTrue(p.hasAirportTransfer());
    }

    @Test
    void stepOrderDoesNotMatter() {
        assertDoesNotThrow(valid().hotel("Rixos", 5).budget(BudgetLevel.PREMIUM)::build);
    }

    @Test
    void rejectsBadArgumentsImmediately() {
        assertThrows(IllegalArgumentException.class, () -> TravelPackage.builder(" ", ALMATY, ASTANA));
        assertThrows(IllegalArgumentException.class, () -> valid().travelers(0, 1));
        assertThrows(IllegalArgumentException.class, () -> valid().hotel("Hilton", 6));
        assertThrows(NullPointerException.class, () -> valid().budget(null));
    }

    // ---- Rules ----

    @ParameterizedTest
    @CsvSource({"0, false", "1, true", "30, true", "31, false"})
    void nightsRange(int nights, boolean ok) {
        if (ok) {
            assertDoesNotThrow(valid().nights(nights)::build);
        } else {
            assertError(valid().nights(nights), "Nights");
        }
    }

    @Test
    void hotelIsRequired() {
        assertError(TravelPackage.builder("T", ALMATY, ASTANA).nights(3).train("002"), "Hotel is required");
    }

    @ParameterizedTest
    @CsvSource({"ECONOMY, 3, true", "ECONOMY, 4, false", "STANDARD, 2, false", "PREMIUM, 4, true", "PREMIUM, 3, false"})
    void hotelStarsDependOnBudget(BudgetLevel budget, int stars, boolean ok) {
        var builder = valid().budget(budget).hotel("H", stars);
        if (ok) {
            assertDoesNotThrow(builder::build);
        } else {
            assertError(builder, "budget allows");
        }
    }

    @Test
    void flightAndTrainAreMutuallyExclusive() {
        assertError(valid().flight("KC 1"), "exactly one transport");
        assertError(TravelPackage.builder("T", ALMATY, ASTANA).nights(3).hotel("H", 4), "exactly one transport");
    }

    @Test
    void internationalTripNeedsVisaSupport() {
        var abroad = TravelPackage.builder("T", ALMATY, ANTALYA).nights(3).hotel("H", 4).flight("KC 921");

        assertError(abroad, "visa");
        assertDoesNotThrow(abroad.visaSupport()::build);
    }

    @Test
    void childrenNeedFamilyRoom() {
        assertError(valid().room(RoomType.SUITE).travelers(1, 1), "FAMILY room");
        assertDoesNotThrow(valid().room(RoomType.FAMILY).travelers(2, 2)::build);
    }

    @ParameterizedTest
    @CsvSource({"STANDARD, 2, true", "STANDARD, 3, false", "FAMILY, 4, true", "FAMILY, 5, false"})
    void guestsMustFitRoom(RoomType room, int adults, boolean ok) {
        var builder = valid().room(room).travelers(adults, 0);
        if (ok) {
            assertDoesNotThrow(builder::build);
        } else {
            assertError(builder, "fits");
        }
    }

    @Test
    void airportTransferNeedsFlight() {
        assertError(valid().airportTransfer(), "Airport transfer");
    }

    @Test
    void reportsAllErrorsAtOnce() {
        var builder = TravelPackage.builder("BAD", ALMATY, ANTALYA)
                .nights(45).budget(BudgetLevel.ECONOMY).hotel("Palace", 5)
                .travelers(2, 2).train("002").airportTransfer();

        var e = assertThrows(InvalidTravelPackageException.class, builder::build);
        assertEquals(6, e.errors().size(), e.errors().toString());
    }

    // ---- Preset ----

    @Test
    void beachPresetWorksAfterAddingHotelAndFlight() {
        TravelPackage p = TravelPresets.allInclusiveBeach("B", ALMATY, ANTALYA)
                .hotel("Rixos", 5).flight("KC 921").visaSupport()
                .build();

        assertEquals(7, p.nights());
        assertEquals(BudgetLevel.PREMIUM, p.budget());
        assertEquals(MealPlan.ALL_INCLUSIVE, p.meals());
        assertTrue(p.hasAirportTransfer());
    }

    @Test
    void presetCanBeChangedButIsStillValidated() {
        var preset = TravelPresets.allInclusiveBeach("B", ALMATY, ANTALYA).flight("KC 921").visaSupport();

        assertDoesNotThrow(preset.nights(14).hotel("Rixos", 5)::build);
        assertError(preset.hotel("Cheap Inn", 2), "budget allows");
    }
}
