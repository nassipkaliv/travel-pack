package kz.aitu.sdp.travel;

public record Hotel(String name, int stars) {
    public Hotel {
        if (name == null || name.isBlank() || stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Hotel needs a name and 1..5 stars");
        }
    }
}
