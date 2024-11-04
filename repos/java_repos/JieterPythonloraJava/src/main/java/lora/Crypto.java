package lora;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class Crypto {
    public static final int UP_LINK = 0;
    public static final int DOWN_LINK = 1;

    public static byte[] loramacDecrypt(String payloadHex, int sequenceCounter, String keyHex, String devAddrHex, int direction) throws Exception {
        byte[] key = hexStringToByteArray(keyHex);
        byte[] devAddr = hexStringToByteArray(devAddrHex);
        byte[] buffer = hexStringToByteArray(payloadHex);
        int size = buffer.length;

        int bufferIndex = 0;
        int ctr = 1;

        byte[] encBuffer = new byte[size];

        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        // Define AES encryption block
        byte[] aBlock = new byte[] {
            0x01, 0x00, 0x00, 0x00, 0x00, (byte) direction,
            devAddr[3], devAddr[2], devAddr[1], devAddr[0],
            (byte) (sequenceCounter & 0xFF),
            (byte) ((sequenceCounter >> 8) & 0xFF),
            (byte) ((sequenceCounter >> 16) & 0xFF),
            (byte) ((sequenceCounter >> 24) & 0xFF),
            0x00, 0x00
        };

        while (size >= 16) {
            aBlock[15] = (byte) (ctr & 0xFF);
            ctr += 1;
            byte[] sBlock = aesEncryptBlock(cipher, keySpec, aBlock);
            for (int i = 0; i < 16; i++) {
                encBuffer[bufferIndex + i] = (byte) (buffer[bufferIndex + i] ^ sBlock[i]);
            }
            size -= 16;
            bufferIndex += 16;
        }

        if (size > 0) {
            aBlock[15] = (byte) (ctr & 0xFF);
            byte[] sBlock = aesEncryptBlock(cipher, keySpec, aBlock);
            for (int i = 0; i < size; i++) {
                encBuffer[bufferIndex + i] = (byte) (buffer[bufferIndex + i] ^ sBlock[i]);
            }
        }

        return encBuffer;
    }

    public static byte[] aesEncryptBlock(Cipher cipher, SecretKeySpec keySpec, byte[] aBlock) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(aBlock);
    }

    public static String generateAppsKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[16];
        random.nextBytes(key);
        StringBuilder sb = new StringBuilder();
        for (byte b : key) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
