package kz.aitu.sdp.travel;

public record Train(String number) {
    public Train {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Train number must not be blank");
        }
    }
}
