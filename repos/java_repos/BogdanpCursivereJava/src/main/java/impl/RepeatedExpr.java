public class RepeatedExpr extends RegexExpr {
    private final RegexExpr expr;
    private final int exactly;
    private final int atLeast;
    private final int atMost;
    private final boolean isExact;

    public RepeatedExpr(RegexExpr expr, int exactly) {
        this.expr = expr;
        this.exactly = exactly;
        this.atLeast = -1;
        this.atMost = -1;
        this.isExact = true;
    }

    public RepeatedExpr(RegexExpr expr, int atLeast, int atMost) {
        this.expr = expr;
        this.exactly = -1;
        this.atLeast = atLeast;
        this.atMost = atMost;
        this.isExact = false;
    }

    public RepeatedExpr(RegexExpr expr, int atLeast, int atMost, boolean greedy) {
        this.expr = expr;
        this.exactly = -1;
        this.atLeast = atLeast;
        this.atMost = atMost;
        this.isExact = !greedy;
    }

    @Override
    public String toString() {
        if (isExact) {
            return "(?:" + expr.toString() + "){" + exactly + "}";
        } else if (atLeast == 0 && atMost != -1) {
            return "(?:" + expr.toString() + "){0," + atMost + "}" + (isExact ? "?" : "");
        } else if (atLeast != -1 && atMost == -1) {
            return "(?:" + expr.toString() + "){" + atLeast + ",}" + (isExact ? "?" : "");
        } else {
            return "(?:" + expr.toString() + "){" + atLeast + "," + atMost + "}" + (isExact ? "?" : "");
        }
    }
}
