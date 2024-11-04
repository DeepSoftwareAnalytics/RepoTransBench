import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FFX {
    private byte[] key;
    private int rounds;
    private MessageDigest digest;

    private static final int DEFAULT_ROUNDS = 10;

    public FFX(byte[] key) {
        this(key, DEFAULT_ROUNDS, "SHA-1");
    }

    public FFX(byte[] key, int rounds, String algorithm) {
        this.key = key;
        this.rounds = rounds;
        try {
            this.digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Invalid algorithm: " + algorithm);
        }
    }

    private List<Integer> add(int radix, List<Integer> a, List<Integer> b) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            result.add((a.get(i) + b.get(i)) % radix);
        }
        return result;
    }

    private List<Integer> sub(int radix, List<Integer> a, List<Integer> b) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            result.add((a.get(i) - b.get(i) + radix) % radix);
        }
        return result;
    }

    private List<Integer> round(int radix, int i, List<Integer> s) {
        try {
            byte[] keyBytes = ByteBuffer.allocate(4 + s.size() * 4).putInt(i).array();
            for (int x : s) {
                keyBytes = ByteBuffer.allocate(keyBytes.length + 4).put(keyBytes).putInt(x).array();
            }
            
            // Fix: Use the correct HMAC algorithm
            String hmacAlgorithm = "Hmac" + digest.getAlgorithm().replace("-", "");
            Mac mac = Mac.getInstance(hmacAlgorithm);
            mac.init(new SecretKeySpec(key, hmacAlgorithm));
            
            List<Integer> result = new ArrayList<>();
            int charsPerHash = (int) Math.floor(digest.getDigestLength() * (Math.log(256) / Math.log(radix)));
            int counter = 0;
            while (result.size() < s.size()) {
                byte[] hmacResult = mac.doFinal(ByteBuffer.allocate(keyBytes.length + 4).put(keyBytes).putInt(counter++).array());
                ByteBuffer buffer = ByteBuffer.wrap(hmacResult);
                while (buffer.hasRemaining() && result.size() < s.size()) {
                    int d = buffer.get() & 0xFF;
                    for (int j = 0; j < charsPerHash && result.size() < s.size(); j++) {
                        result.add(d % radix);
                        d /= radix;
                    }
                }
                mac.reset();
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Fail to compute round", e);
        }
    }

    private List<Integer> split(List<Integer> v) {
        return v.subList(0, v.size() / 2);
    }

    public List<Integer> encrypt(int radix, List<Integer> v) {
        List<Integer> a = split(v);
        List<Integer> b = v.subList(a.size(), v.size());
        for (int i = 0; i < rounds; i++) {
            List<Integer> c = add(radix, a, round(radix, i, b));
            a = b;
            b = c;
        }
        List<Integer> result = new ArrayList<>(a);
        result.addAll(b);
        return result;
    }

    public List<Integer> decrypt(int radix, List<Integer> v) {
        List<Integer> a = split(v);
        List<Integer> b = v.subList(a.size(), v.size());
        for (int i = rounds - 1; i >= 0; i--) {
            List<Integer> c = a;
            a = sub(radix, b, round(radix, i, c));
            b = c;
        }
        List<Integer> result = new ArrayList<>(a);
        result.addAll(b);
        return result;
    }
}
