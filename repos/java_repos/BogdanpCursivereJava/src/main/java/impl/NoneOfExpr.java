public class NoneOfExpr extends RegexExpr {
    private final String expr;

    public NoneOfExpr(String expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        return "[^" + expr + "]";
    }
}
