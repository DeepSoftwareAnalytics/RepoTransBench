public class ZeroOrMoreExpr extends RegexExpr {
    private final RegexExpr expr;

    public ZeroOrMoreExpr(RegexExpr expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        return expr.toString() + "*";
    }
}
