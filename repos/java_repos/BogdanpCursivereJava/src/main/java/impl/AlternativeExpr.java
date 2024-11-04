import java.util.Arrays;
import java.util.stream.Collectors;

public class AlternativeExpr extends RegexExpr {
    private final RegexExpr[] exprs;

    public AlternativeExpr(RegexExpr... exprs) {
        this.exprs = exprs;
    }

    @Override
    public String toString() {
        return Arrays.stream(exprs)
                .map(RegexExpr::toString)
                .collect(Collectors.joining("|"));
    }
}
