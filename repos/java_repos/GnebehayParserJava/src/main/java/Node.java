import java.util.ArrayList;
import java.util.List;

public class Node {
    TokenType tokenType;
    Integer value;
    List<Node> children;

    public Node(TokenType tokenType, Integer value) {
        this.tokenType = tokenType;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public Node(TokenType tokenType) {
        this(tokenType, null);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public Integer getValue() {
        return value;
    }

    public List<Node> getChildren() {
        return children;
    }
}
