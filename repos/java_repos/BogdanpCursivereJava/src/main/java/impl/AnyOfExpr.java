public class AnyOfExpr extends RegexExpr {
    private final String expr;

    public AnyOfExpr(String expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        return "[" + expr + "]";
    }
}
