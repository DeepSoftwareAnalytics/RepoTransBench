public class MaybeExpr extends RegexExpr {
    private final RegexExpr expr;

    public MaybeExpr(RegexExpr expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        return expr.toString() + "?";
    }
}
