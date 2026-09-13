package kz.aitu.sdp.travel;

public enum RoomType {
    STANDARD(2),
    SUITE(3),
    FAMILY(4);

    private final int capacity;

    RoomType(int capacity) {
        this.capacity = capacity;
    }

    public int capacity() { return capacity; }
}
