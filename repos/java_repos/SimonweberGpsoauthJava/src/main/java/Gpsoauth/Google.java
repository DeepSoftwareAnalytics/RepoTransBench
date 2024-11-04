package Gpsoauth;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.UrlBase64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;

public class Google {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PublicKey keyFromB64(String b64Key) throws Exception {
        byte[] binaryKey = Base64.decode(b64Key);
        byte[] modBytes = subset(binaryKey, 4, Util.bytesToInt(subset(binaryKey, 0, 4)));
        byte[] expBytes = subset(binaryKey, 8 + modBytes.length, Util.bytesToInt(subset(binaryKey, 4 + modBytes.length, 4)));

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new java.math.BigInteger(modBytes), new java.math.BigInteger(expBytes));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public static byte[] subset(byte[] array, int start, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, start, result, 0, length);
        return result;
    }

    public static Map<String, String> parseAuthResponse(String text) {
        Map<String, String> responseData = new HashMap<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] keyVal = line.split("=");
            responseData.put(keyVal[0], keyVal[1]);
        }

        return responseData;
    }

    public static String constructSignature(String email, String password, PublicKey key) throws Exception {
        byte[] toSign = (email + "\u0000" + password).getBytes("UTF-8");
        byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(toSign);

        Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(toSign);

        byte[] signature = new byte[1 + 4 + encrypted.length];
        signature[0] = 0;
        System.arraycopy(sha1, 0, signature, 1, 4);
        System.arraycopy(encrypted, 0, signature, 5, encrypted.length);

        return new String(UrlBase64.encode(signature));
    }
}
