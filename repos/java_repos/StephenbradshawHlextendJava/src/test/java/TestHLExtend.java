import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.lang.CloneNotSupportedException;


public class TestHLExtend {

    private byte[][] reference;
    private String[] algorithms = HLExtend.getAlgorithmsAll();

    @BeforeEach
    public void setUp() {
        reference = new byte[][] {
            "abc".getBytes(),
            "The quick brown fox jumped over the lazy dog".getBytes(),
            "The quick brown fox jumped over the lazy dog.".getBytes(),
            "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".getBytes(),
            new byte[30]
        };
        new Random().nextBytes(reference[4]);
    }

    @Test
    public void testComparativeHashGeneration() throws NoSuchAlgorithmException,CloneNotSupportedException {
        for (String alg : algorithms) {
            for (int a = 0; a < 256; a++) {
                byte[] string = new byte[a];
                for (int i = 0; i < a; i++) {
                    string[i] = 'A';
                }

                MessageDigest h = MessageDigest.getInstance(alg);
                h.update(string);
                String test1 = bytesToHex(h.digest());

                HLExtend s = new HLExtend(alg);
                s.hash(string);
                String test2 = bytesToHex(s.digest());

                assertEquals(test1, test2, alg + " no match for string of length " + a);
            }
        }
    }

    @Test
    public void testReferenceHashValues() throws NoSuchAlgorithmException,CloneNotSupportedException {
        for (String alg : algorithms) {
            for (byte[] ref : reference) {
                HLExtend s = new HLExtend(alg);
                s.hash(ref);
                MessageDigest h = MessageDigest.getInstance(alg);
                h.update(ref);
                String hhex = bytesToHex(h.digest());
                String shex = bytesToHex(s.digest());

                assertEquals(hhex, shex, "Reference value check failed for " + alg + " on value " + new String(ref));
            }
        }
    }

    @Test
    public void testHashLengthExtension() throws NoSuchAlgorithmException,CloneNotSupportedException {
        for (String alg : algorithms) {
            System.out.println("Testing extension function for " + alg);

            for (int secLen = 10; secLen < 130; secLen += 20) {
                byte[] secret = new byte[secLen];
                for (int i = 0; i < secLen; i++) {
                    secret[i] = 'B';
                }

                for (int knownLen = 60; knownLen < 130; knownLen++) {
                    byte[] known = new byte[knownLen];
                    for (int i = 0; i < knownLen; i++) {
                        known[i] = 'A';
                    }

                    for (int appendLen = 10; appendLen < 50; appendLen += 10) {
                        byte[] append = new byte[appendLen];
                        for (int i = 0; i < appendLen; i++) {
                            append[i] = 'C';
                        }

                        MessageDigest sh = MessageDigest.getInstance(alg);
                        sh.update(secret);
                        sh.update(known);
                        String startHash = bytesToHex(sh.digest());

                        HLExtend s = new HLExtend(alg);
                        byte[] appendVal = s.extend(append, known, secLen, startHash);
                        String appendVal1 = bytesToPrintableString(appendVal);
                        String appendHash = bytesToHex(s.digest());

                        sh.update(appendVal);
                        String newHash = bytesToHex(sh.digest());

                        if (!appendHash.equals(newHash)) {
                            HLExtend gh = new HLExtend(alg);
                            gh.hash(arrayConcat(secret, appendVal));
                            System.out.println("Algorithm: " + alg);
                            System.out.println("Start hash: " + startHash);
                            System.out.println("Append hash: " + appendHash);
                            System.out.println("New hash: " + newHash);
                            System.out.println("Secret: " + new String(secret));
                            System.out.println("Known: " + new String(known));
                            System.out.println("Base append: " + new String(append));
                            System.out.println(appendVal1);
                            throw new AssertionError("Verification failure");
                        }
                    }
                }
            }
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String bytesToPrintableString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if (b < 0x20 || b > 0x7e) {
                sb.append(String.format("\\x%02x", b));
            } else {
                sb.append((char) b);
            }
        }
        return sb.toString();
    }

    private byte[] arrayConcat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
