import java.util.Base64;
import java.nio.ByteBuffer;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MintOTP {

    public static String hotp(String key, long counter, int digits, String digest) {
        byte[] keyBytes = Base64.getDecoder().decode(key.toUpperCase());
        byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();

        try {
            Mac mac = Mac.getInstance(digest);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, digest);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(counterBytes);

            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ByteBuffer.wrap(hash, offset, 4).getInt() & 0x7fffffff;
            String otp = Integer.toString(binary % (int) Math.pow(10, digits));
            return String.format("%0" + digits + "d", Integer.parseInt(otp));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static String hotp(String key, long counter) {
        return hotp(key, counter, 6, "HmacSHA1");
    }

    public static String totp(String key, long timeStep, int digits, String digest) {
        return hotp(key, System.currentTimeMillis() / timeStep / 1000, digits, digest);
    }

    public static String totp(String key) {
        return totp(key, 30, 6, "HmacSHA1");
    }

    public static void main(String[] args) {
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            int timeStep = args.length > 0 ? Integer.parseInt(args[0]) : 30;
            int digits = args.length > 1 ? Integer.parseInt(args[1]) : 6;
            String digest = args.length > 2 ? "Hmac" + args[2].toUpperCase() : "HmacSHA1";
            while (scanner.hasNext()) {
                System.out.println(totp(scanner.nextLine().trim(), timeStep, digits, digest));
            }
        }
    }
}
