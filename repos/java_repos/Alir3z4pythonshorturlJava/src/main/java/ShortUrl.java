import java.util.Map;
import java.util.HashMap;

public class ShortUrl {

    private static final String DEFAULT_ALPHABET = "mn6j2c4rv8bpygw95z7hsdaetxuk3fq";
    private static final int MIN_LENGTH = 22; // Update based on test expectations
    private static final UrlEncoder DEFAULT_ENCODER = new UrlEncoder(DEFAULT_ALPHABET);

    public static class UrlEncoder {
        private final String alphabet;
        private final Map<Character, Integer> alphabetMap;
        private final int base;

        public UrlEncoder(String alphabet) {
            if (alphabet.chars().distinct().count() < 2) {
                throw new IllegalArgumentException("Alphabet has to contain at least 2 characters.");
            }
            this.alphabet = alphabet;
            this.base = alphabet.length();
            this.alphabetMap = new HashMap<>();
            for (int i = 0; i < alphabet.length(); i++) {
                alphabetMap.put(alphabet.charAt(i), i);
            }
        }

        public String encodeUrl(int n, int minLength) {
            return enbase(n, minLength);
        }

        public String encodeUrl(int n) {
            return encodeUrl(n, MIN_LENGTH);
        }

        public int decodeUrl(String n) {
            return debase(n);
        }

        private String enbase(int x, int minLength) {
            StringBuilder result = new StringBuilder();
            while (x > 0) {
                result.insert(0, alphabet.charAt(x % base));
                x /= base;
            }
            // Pad the result with the first character of the alphabet to ensure fixed length
            while (result.length() < minLength) {
                result.insert(0, alphabet.charAt(0));
            }
            return result.toString();
        }

        private int debase(String x) {
            int n = base;
            int result = 0;
            for (char c : x.toCharArray()) {
                result = result * n + alphabetMap.get(c);
            }
            return result;
        }
    }

    public static String encode(int n) {
        return DEFAULT_ENCODER.encodeUrl(n);
    }

    public static int decode(String n) {
        return DEFAULT_ENCODER.decodeUrl(n);
    }

    public static String enbase(int n, int minLength) {
        return DEFAULT_ENCODER.enbase(n, minLength);
    }

    public static String enbase(int n) {
        return DEFAULT_ENCODER.enbase(n, MIN_LENGTH);
    }

    public static int debase(String n) {
        return DEFAULT_ENCODER.debase(n);
    }

    public static String encodeUrl(int n, int minLength) {
        return DEFAULT_ENCODER.encodeUrl(n, minLength);
    }

    public static String encodeUrl(int n) {
        return DEFAULT_ENCODER.encodeUrl(n);
    }

    public static int decodeUrl(String n) {
        return DEFAULT_ENCODER.decodeUrl(n);
    }
}
