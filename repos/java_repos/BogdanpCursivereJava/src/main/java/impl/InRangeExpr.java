public class InRangeExpr extends RegexExpr {
    private final String lo;
    private final String hi;

    public InRangeExpr(String lo, String hi) {
        this.lo = lo;
        this.hi = hi;
    }

    @Override
    public String toString() {
        return lo + "-" + hi;
    }
}
