public class LiteralExpr extends RegexExpr {
    private final String literal;

    public LiteralExpr(String literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        return this.literal;
    }
}
