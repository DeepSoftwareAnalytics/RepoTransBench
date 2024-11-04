import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class ParserTest {
    @Test
    void testLexicalAnalysis() throws Exception {
        List<Node> tokens = Parser.lexicalAnalysis("1+2");
        assertEquals(4, tokens.size());
        assertEquals(TokenType.T_NUM, tokens.get(0).getTokenType());
        assertEquals(TokenType.T_PLUS, tokens.get(1).getTokenType());
        assertEquals(TokenType.T_NUM, tokens.get(2).getTokenType());
        assertEquals(TokenType.T_END, tokens.get(3).getTokenType());

        tokens = Parser.lexicalAnalysis("3-2");
        assertEquals(4, tokens.size());
        assertEquals(TokenType.T_NUM, tokens.get(0).getTokenType());
        assertEquals(TokenType.T_MINUS, tokens.get(1).getTokenType());
        assertEquals(TokenType.T_NUM, tokens.get(2).getTokenType());
        assertEquals(TokenType.T_END, tokens.get(3).getTokenType());

        tokens = Parser.lexicalAnalysis("5*6");
        assertEquals(4, tokens.size());
        assertEquals(TokenType.T_NUM, tokens.get(0).getTokenType());
        assertEquals(TokenType.T_MULT, tokens.get(1).getTokenType());
        assertEquals(TokenType.T_NUM, tokens.get(2).getTokenType());
        assertEquals(TokenType.T_END, tokens.get(3).getTokenType());

        tokens = Parser.lexicalAnalysis("8/4");
        assertEquals(4, tokens.size());
    }
}
