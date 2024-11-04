import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringCodecTest {
    @Test
    public void testEncrypt() {
        StringCodec s = new StringCodec(new FFX("foo".getBytes()), "abc", 3);
        Assertions.assertThrows(IllegalArgumentException.class, () -> s.encrypt("abx"));
        Assertions.assertEquals("abb", s.encrypt("cba"));
        Assertions.assertEquals("ccc", s.decrypt(s.encrypt("ccc")));
    }
}
