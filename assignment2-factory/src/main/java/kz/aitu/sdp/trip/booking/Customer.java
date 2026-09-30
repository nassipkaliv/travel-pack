package kz.aitu.sdp.trip.booking;

import java.util.Optional;

/** Who is booking. {@code company} is set only for corporate bookings. */
public record Customer(String name, int travelers, String contact, String company) {

    public Customer {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name must not be blank");
        }
        if (travelers < 1) {
            throw new IllegalArgumentException("At least one traveler is required");
        }
    }

    public static Customer person(String name, int travelers, String contact) {
        return new Customer(name, travelers, contact, null);
    }

    public Optional<String> companyName() {
        return Optional.ofNullable(company);
    }
}
