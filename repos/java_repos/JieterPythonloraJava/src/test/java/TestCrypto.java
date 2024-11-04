import lora.Crypto;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCrypto {

    @Test
    public void testLoramacDecrypt() throws Exception {
        String key = "271E403DF4225EEF7E90836494A5B345";
        String devAddr = "000015E4";

        // 定义测试数据 (sequence_counter, payload_hex)
        Object[][] payloads = {
            {0, "73100b90"},
            {1, "68d388f0"},
            {2, "0a12e808"},
            {3, "e3413bee"}
        };
        String expected = "cafebabe";

        for (Object[] payload : payloads) {
            int sequenceCounter = (int) payload[0];
            String payloadHex = (String) payload[1];
            byte[] plaintextInts = Crypto.loramacDecrypt(payloadHex, sequenceCounter, key, devAddr, Crypto.UP_LINK);

            // 将解密后的字节数组转换为十六进制字符串
            StringBuilder plaintextHex = new StringBuilder();
            for (byte b : plaintextInts) {
                plaintextHex.append(String.format("%02x", b));
            }

            // 断言解密结果是否符合预期
            assertEquals(expected, plaintextHex.toString());
        }
    }

    @Test
    public void testAppskey() {
        String key = Crypto.generateAppsKey();

        // 断言生成的密钥长度为32字符 (16字节)
        assertEquals(32, key.length());

        // 断言生成的密钥是随机的，每次生成不同
        assertNotEquals(key, Crypto.generateAppsKey());
        assertNotEquals(Crypto.generateAppsKey(), Crypto.generateAppsKey());
    }
}
