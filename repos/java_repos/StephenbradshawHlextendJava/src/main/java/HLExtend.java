import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class HLExtend {

    private static List<String> supportedAlgorithms = Arrays.asList("MD5", "SHA-1", "SHA-256", "SHA-512");
    private MessageDigest messageDigest;

    public HLExtend(String algorithm) throws NoSuchAlgorithmException {
        if (!supportedAlgorithms.contains(algorithm)) {
            throw new NoSuchAlgorithmException("Unsupported algorithm: " + algorithm);
        }
        this.messageDigest = MessageDigest.getInstance(algorithm);
    }

    public void hash(byte[] input) {
        messageDigest.update(input);
    }

    public byte[] digest() {
        return messageDigest.digest();
    }

    public static String[] getAlgorithmsAll() {
        return supportedAlgorithms.toArray(new String[0]);
    }

    // Method to perform hash length extension attack
    public byte[] extend(byte[] append, byte[] known, int secretLen, String startHash) 
            throws NoSuchAlgorithmException, CloneNotSupportedException {
        // Initialize the hash algorithm state to the given intermediate hash.
        setStartingHash(startHash);

        // Perform the hash length extension attack.
        int extendLength = getExtendLength(secretLen, known, append);

        byte[] message = append;
        for (int i = 0; i < message.length; i += messageDigest.getDigestLength()) {
            int chunkLength = Math.min(messageDigest.getDigestLength(), message.length - i);
            messageDigest.update(Arrays.copyOfRange(message, i, i + chunkLength));
        }

        byte[] padding = getHashBinaryPad(secretLen, known);
        messageDigest.update(padding);
        return arrayConcat(arrayConcat(known, padding), message);
    }

    private int getExtendLength(int secretLen, byte[] known, byte[] append) {
        int originalLength = (secretLen + known.length + 1 + messageDigest.getDigestLength()) * 8;
        return originalLength + (append.length * 8);
    }

    private byte[] getHashBinaryPad(int secretLen, byte[] known) {
        int padLength = messageDigest.getDigestLength() - ((known.length + secretLen) % messageDigest.getDigestLength());
        byte[] padding = new byte[padLength];
        padding[0] = (byte) 0x80;
        return padding;
    }

    private byte[] arrayConcat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private void setStartingHash(String startHash) {
        byte[] hashBytes = hexStringToByteArray(startHash);
        if (hashBytes == null) {
            throw new IllegalArgumentException("Starting hash cannot be null or empty");
        }
        messageDigest.update(hashBytes);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                              + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
