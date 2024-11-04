public class SequenceExpr extends RegexExpr {
    private final RegexExpr[] exprs;

    public SequenceExpr(RegexExpr... exprs) {
        this.exprs = exprs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RegexExpr expr : exprs) {
            sb.append(expr.toString());
        }
        return sb.toString();
    }
}
