public class OneOrMoreExpr extends RegexExpr {
    private final RegexExpr expr;

    public OneOrMoreExpr(RegexExpr expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        return expr.toString() + "+";
    }
}
