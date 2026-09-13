package kz.aitu.sdp.travel;

public record Flight(String number) {
    public Flight {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Flight number must not be blank");
        }
    }
}
