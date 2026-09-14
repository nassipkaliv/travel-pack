package kz.aitu.sdp.travel;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;

/** Reads typed values from the console. Empty input (Enter) means "skip". */
final class ConsoleInput {

    private final Scanner scanner;
    private final PrintStream out;

    ConsoleInput(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
    }

    String text(String prompt) {
        out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    /** @param ifEmpty what pressing Enter means, shown to the user: "none", "1", "keep" */
    Optional<String> optionalText(String prompt, String ifEmpty) {
        String value = text(prompt + " [Enter = " + ifEmpty + "]");
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    Optional<Integer> optionalNumber(String prompt, String ifEmpty) {
        while (true) {
            Optional<String> value = optionalText(prompt, ifEmpty);
            if (value.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of(Integer.parseInt(value.get()));
            } catch (NumberFormatException e) {
                out.println("  Please enter a whole number.");
            }
        }
    }

    int number(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(text(prompt));
            } catch (NumberFormatException e) {
                out.println("  Please enter a whole number.");
            }
        }
    }

    boolean yesNo(String prompt) {
        String answer = text(prompt + " (y/n) [Enter = n]").toLowerCase();
        return answer.equals("y") || answer.equals("yes");
    }

    /** City and country in one line: "Almaty, Kazakhstan". Asks again until the format is right. */
    Location location(String prompt) {
        while (true) {
            String[] parts = text(prompt + " (City, Country)").split(",");
            if (parts.length == 2) {
                try {
                    return new Location(parts[0].trim(), parts[1].trim());
                } catch (IllegalArgumentException e) {
                    // falls through to the hint below
                }
            }
            out.println("  Format: City, Country — for example: Almaty, Kazakhstan");
        }
    }

    <E extends Enum<E>> Optional<E> optionalChoice(String prompt, E[] values, Function<E, String> hint) {
        out.println(prompt + ":");
        for (E value : values) {
            out.printf("  %d) %-14s %s%n", value.ordinal() + 1, value, hint.apply(value));
        }
        while (true) {
            Optional<Integer> number = optionalNumber("  Choose 1-" + values.length, "keep");
            if (number.isEmpty()) {
                return Optional.empty();
            }
            if (number.get() >= 1 && number.get() <= values.length) {
                return Optional.of(values[number.get() - 1]);
            }
            out.println("  No such option.");
        }
    }
}
