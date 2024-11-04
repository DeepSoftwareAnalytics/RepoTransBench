import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public static List<Node> lexicalAnalysis(String s) throws Exception {
        List<Node> tokens = new ArrayList<>();
        for (char c : s.toCharArray()) {
            if (c == '+') {
                tokens.add(new Node(TokenType.T_PLUS, (int) c));
            } else if (c == '-') {
                tokens.add(new Node(TokenType.T_MINUS, (int) c));
            } else if (c == '*') {
                tokens.add(new Node(TokenType.T_MULT, (int) c));
            } else if (c == '/') {
                tokens.add(new Node(TokenType.T_DIV, (int) c));
            } else if (c == '(') {
                tokens.add(new Node(TokenType.T_LPAR, (int) c));
            } else if (c == ')') {
                tokens.add(new Node(TokenType.T_RPAR, (int) c));
            } else if (Character.isDigit(c)) {
                tokens.add(new Node(TokenType.T_NUM, Character.getNumericValue(c)));
            } else {
                throw new Exception("Invalid token: " + c);
            }
        }
        tokens.add(new Node(TokenType.T_END));
        return tokens;
    }

    private static Node match(List<Node> tokens, TokenType tokenType) throws Exception {
        if (tokens.get(0).getTokenType() == tokenType) {
            return tokens.remove(0);
        } else {
            throw new Exception("Invalid syntax on token " + tokens.get(0).getTokenType());
        }
    }

    private static Node parseE(List<Node> tokens) throws Exception {
        Node leftNode = parseE2(tokens);

        while (tokens.get(0).getTokenType() == TokenType.T_PLUS || tokens.get(0).getTokenType() == TokenType.T_MINUS) {
            Node node = tokens.remove(0);
            node.children.add(leftNode);
            node.children.add(parseE2(tokens));
            leftNode = node;
        }

        return leftNode;
    }

    private static Node parseE2(List<Node> tokens) throws Exception {
        Node leftNode = parseE3(tokens);

        while (tokens.get(0).getTokenType() == TokenType.T_MULT || tokens.get(0).getTokenType() == TokenType.T_DIV) {
            Node node = tokens.remove(0);
            node.children.add(leftNode);
            node.children.add(parseE3(tokens));
            leftNode = node;
        }

        return leftNode;
    }

    private static Node parseE3(List<Node> tokens) throws Exception {
        if (tokens.get(0).getTokenType() == TokenType.T_NUM) {
            return tokens.remove(0);
        }

        match(tokens, TokenType.T_LPAR);
        Node expression = parseE(tokens);
        match(tokens, TokenType.T_RPAR);

        return expression;
    }

    public static Node parse(String inputString) throws Exception {
        List<Node> tokens = lexicalAnalysis(inputString);
        Node ast = parseE(tokens);
        match(tokens, TokenType.T_END);
        return ast;
    }
}
