import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class TestGpsoauth {

    private static final String ANDROID_KEY_7_3_29 = "some-android-key";
    private static final String B64_KEY_7_3_29 = "some-base64-key";

    private static byte[] constructSignature(String username, String password, String key) {
        try {
            String data = username + ":" + password;
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            Cipher cipher = Cipher.getInstance("HmacSHA1");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Error while constructing signature", e);
        }
    }

    private static int bytesToInt(byte[] bytes) {
        int result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }

    private static byte[] intToBytes(int value) {
        return new byte[] {
            (byte)(value >> 24),
            (byte)(value >> 16),
            (byte)(value >> 8),
            (byte)value
        };
    }

    @Test
    public void testStaticSignature() {
        String username = "someone@google.com";
        String password = "apassword";

        byte[] signature = constructSignature(username, password, ANDROID_KEY_7_3_29);
        String signatureBase64 = Base64.getEncoder().encodeToString(signature);

        assertTrue(signatureBase64.startsWith("AFcb4K"));
    }

    @Test
    public void testConversionRoundtrip() {
        byte[] keyBytes = Base64.getDecoder().decode(B64_KEY_7_3_29);
        int bytesAsInt = bytesToInt(keyBytes);
        byte[] roundtripBytes = intToBytes(bytesAsInt);

        assertEquals(Base64.getEncoder().encodeToString(roundtripBytes), B64_KEY_7_3_29);
    }
}
