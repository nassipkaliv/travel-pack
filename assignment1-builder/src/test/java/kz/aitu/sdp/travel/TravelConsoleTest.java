package kz.aitu.sdp.travel;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TravelConsoleTest {

    /** Runs the console with the given lines typed by the user and returns what it printed. */
    static String runWith(String... lines) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Scanner scanner = new Scanner(String.join("\n", lines) + "\n");
        new TravelConsole(scanner, new PrintStream(output, true)).run();
        return output.toString();
    }

    @Test
    void createsPackageFromTypedValues() {
        String output = runWith(
                "1",                     // from scratch
                "FAMILY-1",              // id
                "Almaty, Kazakhstan",    // from
                "Astana, Kazakhstan",    // to
                "4",                     // nights
                "",                      // budget: keep STANDARD
                "Hilton", "4",           // hotel
                "3",                     // room: FAMILY
                "",                      // meals: keep
                "2", "2",                // adults, children
                "",                      // no flight
                "002",                   // train
                "n", "n",                // transfer, visa
                "0");                    // exit

        assertTrue(output.contains("Package created"), output);
        assertTrue(output.contains("train 002"), output);
    }

    @Test
    void showsAllErrorsForInvalidPackage() {
        String output = runWith(
                "1", "BAD-1", "Almaty, Kazakhstan", "Antalya, Turkey",
                "45",                    // nights out of range
                "1",                     // ECONOMY
                "Palace", "5",           // 5 stars with ECONOMY
                "", "", "", "",          // room, meals, adults, children
                "", "",                  // no transport
                "n", "n",
                "0");

        assertTrue(output.contains("Package is invalid"), output);
        assertTrue(output.contains("Nights must be 1..30, got 45"), output);
        assertTrue(output.contains("ECONOMY budget allows 1..3 stars, got 5"), output);
        assertTrue(output.contains("International trip requires visa support"), output);
    }

    @Test
    void rejectsBadValueRightAway() {
        String output = runWith("1", "", "Almaty, Kazakhstan", "Astana, Kazakhstan", "0");

        assertTrue(output.contains("Rejected right away: id must not be blank"), output);
    }
}
