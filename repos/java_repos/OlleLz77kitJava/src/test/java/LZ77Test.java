import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LZ77Test {

    private String textData;
    private String encodedTextData;
    private Compressor compressor;

    @BeforeEach
    public void setUp() {
        textData = "ababassbabasbabbbaababassbababsbasbasbbabbbababababaaaaabbbab";
        encodedTextData = "ababassbabasbabbba` '&bs` 1 sb` 4!` . abaaaa` *!";
        compressor = new Compressor();
    }

    @Test
    public void testCorrectCompressedOutput() {
        System.out.println("testCorrectCompressedOutput:");
        String result = compressor.compress(textData);
        System.out.println(encodedTextData);
        System.out.println(result);
        assertEquals(encodedTextData, result, "must match");
    }

    @Test
    public void testCorrectDecompressedOutput() {
        System.out.println("testCorrectDecompressedOutput:");
        String result = compressor.decompress(encodedTextData);
        System.out.println(textData);
        System.out.println(result);
        assertEquals(textData, result, "must match");
    }
}
