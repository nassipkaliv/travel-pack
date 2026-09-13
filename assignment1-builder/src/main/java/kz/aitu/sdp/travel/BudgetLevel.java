package kz.aitu.sdp.travel;

public enum BudgetLevel {
    ECONOMY(1, 3),
    STANDARD(3, 4),
    PREMIUM(4, 5);

    private final int minStars;
    private final int maxStars;

    BudgetLevel(int minStars, int maxStars) {
        this.minStars = minStars;
        this.maxStars = maxStars;
    }

    public int minStars() { return minStars; }
    public int maxStars() { return maxStars; }
}
