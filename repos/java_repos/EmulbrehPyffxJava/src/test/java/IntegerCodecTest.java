import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class IntegerCodecTest {
    @Test
    public void testEncrypt() {
        IntegerCodec d = new IntegerCodec(new FFX("foo".getBytes()), 2);
        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(i, d.decrypt(d.encrypt(i)));
        }
        Set<Integer> hist = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            hist.add(d.encrypt(i));
        }
        for (int i = 0; i < 100; i++) {
            Assertions.assertTrue(hist.contains(i));
        }
    }
    
    @Test
    public void testEncryptBigNumber() {
        IntegerCodec d = new IntegerCodec(new FFX("foo".getBytes()), 20);
        Assertions.assertEquals(1, d.decrypt(d.encrypt(1)));
    }

    @Test
    public void testCypherTextWithLeadingZero() {
        IntegerCodec d = new IntegerCodec(new FFX("foo".getBytes()), 2);
        int n = 11;
        int encrypted = d.encrypt(n);
        Assertions.assertTrue(String.valueOf(encrypted).length() <= 2);
        Assertions.assertEquals(n, d.decrypt(encrypted));
    }
}
