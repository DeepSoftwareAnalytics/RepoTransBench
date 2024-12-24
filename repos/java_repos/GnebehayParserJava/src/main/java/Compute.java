import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Compute {
    private static final Map<TokenType, BiFunction<Double, Double, Double>> operations = new HashMap<>();

    static {
        operations.put(TokenType.T_PLUS, Double::sum);
        operations.put(TokenType.T_MINUS, (a, b) -> a - b);
        operations.put(TokenType.T_MULT, (a, b) -> a * b);
        operations.put(TokenType.T_DIV, (a, b) -> a / b);
    }

    public static double compute(Node node) {
        if (node.getTokenType() == TokenType.T_NUM) {
            return node.getValue();
        }
        double leftResult = compute(node.getChildren().get(0));
        double rightResult = compute(node.getChildren().get(1));
        BiFunction<Double, Double, Double> operation = operations.get(node.getTokenType());
        return operation.apply(leftResult, rightResult);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Please provide one argument");
        }
        Node ast = Parser.parse(args[0]);
        double result = compute(ast);
        System.out.println(result);
    }
}
