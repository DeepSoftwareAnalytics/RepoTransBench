public class GroupExpr extends RegexExpr {
    private final RegexExpr expr;
    private final String name;
    private final boolean capture;

    public GroupExpr(RegexExpr expr, String name, boolean capture) {
        this.expr = expr;
        this.name = name;
        this.capture = capture;
    }

    @Override
    public String toString() {
        if (!capture) {
            return "(?:" + expr.toString() + ")";
        }
        if (name != null) {
            return "(?P<" + name + ">" + expr.toString() + ")";
        }
        return "(" + expr.toString() + ")";
    }
}
