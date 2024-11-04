package shortuuid;

import java.math.BigInteger;
import java.util.*;

public class ShortUUID {
    private List<Character> alphabet;
    private int alphabetLength;

    public ShortUUID() {
        this(null);
    }

    public ShortUUID(String alphabet) {
        if (alphabet == null) {
            alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        }
        setAlphabet(alphabet);
    }

    private int getLength() {
        return (int) Math.ceil(Math.log(Math.pow(2, 128)) / Math.log(alphabetLength));
    }

    public String encode(UUID uuid, Integer padLength) {
        if (uuid == null) {
            throw new IllegalArgumentException("Input uuid must be a UUID object.");
        }
        if (padLength == null) {
            padLength = getLength();
        }
        return intToString(uuid, alphabet, padLength);
    }

    public String encode(UUID uuid) {
        return encode(uuid, null);
    }

    public UUID decode(String str, boolean legacy) {
        if (str == null) {
            throw new IllegalArgumentException("Input string must be a str.");
        }
        if (legacy) {
            str = new StringBuilder(str).reverse().toString();
        }
        return new UUID(new BigInteger(str, alphabetLength).longValue(), new BigInteger(str, alphabetLength).longValue());
    }

    public UUID decode(String str) {
        return decode(str, false);
    }

    public String uuid(String name, Integer padLength) {
        if (padLength == null) {
            padLength = getLength();
        }

        UUID u;
        if (name == null) {
            u = UUID.randomUUID();
        } else {
            u = name.toLowerCase().startsWith("http://") || name.toLowerCase().startsWith("https://")
                    ? UUID.nameUUIDFromBytes(name.getBytes())
                    : UUID.nameUUIDFromBytes(name.getBytes());
        }
        return encode(u, padLength);
    }

    public String uuid(String name) {
        return uuid(name, null);
    }

    public String uuid() {
        return uuid(null, null);
    }

    public String random(Integer length) {
        if (length == null) {
            length = getLength();
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.get(new Random().nextInt(alphabetLength)));
        }
        return sb.toString();
    }

    public String random() {
        return random(null);
    }

    public String getAlphabet() {
        StringBuilder sb = new StringBuilder(alphabet.size());
        for (char c : alphabet) {
            sb.append(c);
        }
        return sb.toString();
    }

    public void setAlphabet(String alphabet) {
        Set<Character> alphabetSet = new TreeSet<>();
        for (char c : alphabet.toCharArray()) {
            alphabetSet.add(c);
        }
        if (alphabetSet.size() > 1) {
            this.alphabet = new ArrayList<>(alphabetSet);
            this.alphabetLength = this.alphabet.size();
        } else {
            throw new IllegalArgumentException("Alphabet with more than one unique symbol required.");
        }
    }

    public int encodedLength(int numBytes) {
        double factor = Math.log(256) / Math.log(alphabetLength);
        return (int) Math.ceil(factor * numBytes);
    }

    public int encodedLength() {
        return encodedLength(16);
    }

    private String intToString(UUID uuid, List<Character> alphabet, int padding) {
        BigInteger number = new BigInteger(uuid.toString().replace("-", ""), 16);
        StringBuilder output = new StringBuilder();
        int alphaLen = alphabet.size();

        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = number.divideAndRemainder(BigInteger.valueOf(alphaLen));
            number = divmod[0];
            output.append(alphabet.get(divmod[1].intValue()));
        }

        if (padding > 0) {
            output.reverse();
            while (output.length() < padding) {
                output.insert(0, alphabet.get(0));
            }
        }
        return output.toString();
    }
}
