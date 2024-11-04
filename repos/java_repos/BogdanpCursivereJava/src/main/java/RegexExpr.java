public abstract class RegexExpr {
    public abstract String toString();

    public RegexExpr or(RegexExpr other) {
        return new AlternativeExpr(this, other);
    }

    public RegexExpr seq(RegexExpr other) {
        return new SequenceExpr(this, other);
    }

    public static RegexExpr beginningOfLine() {
        return new BeginningOfLineExpr();
    }

    public static RegexExpr endOfLine() {
        return new EndOfLineExpr();
    }

    public static RegexExpr anything() {
        return new AnythingExpr();
    }

    public static RegexExpr literal(String literal) {
        return new LiteralExpr(literal);
    }

    public static RegexExpr text(String text) {
        return new TextExpr(text);
    }

    public static RegexExpr anyOf(String chars) {
        return new AnyOfExpr(chars);
    }

    public static RegexExpr noneOf(String chars) {
        return new NoneOfExpr(chars);
    }

    public static RegexExpr inRange(String lo, String hi) {
        return new InRangeExpr(lo, hi);
    }

    public static RegexExpr zeroOrMore(RegexExpr expr) {
        return new ZeroOrMoreExpr(expr);
    }

    public static RegexExpr oneOrMore(RegexExpr expr) {
        return new OneOrMoreExpr(expr);
    }

    public static RegexExpr maybe(RegexExpr expr) {
        return new MaybeExpr(expr);
    }

    public static RegexExpr repeated(RegexExpr expr, int exactly) {
        return new RepeatedExpr(expr, exactly);
    }

    public static RegexExpr repeated(RegexExpr expr, int atLeast, int atMost) {
        return new RepeatedExpr(expr, atLeast, atMost);
    }

    public static RegexExpr repeated(RegexExpr expr, int atLeast, int atMost, boolean greedy) {
        return new RepeatedExpr(expr, atLeast, atMost, greedy);
    }

    public static RegexExpr alternative(RegexExpr... exprs) {
        return new AlternativeExpr(exprs);
    }

    public static RegexExpr group(RegexExpr expr) {
        return new GroupExpr(expr, null, true);
    }

    public static RegexExpr group(RegexExpr expr, String name) {
        return new GroupExpr(expr, name, true);
    }

    public static RegexExpr group(RegexExpr expr, String name, boolean capture) {
        return new GroupExpr(expr, name, capture);
    }
}
