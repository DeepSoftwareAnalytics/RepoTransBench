import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockChainTest {
    private Map<String, Integer> balances;
    private Map<String, String> outputs;

    @BeforeEach
    public void setUp() {
        balances = new HashMap<>();
        outputs = new HashMap<>();
    }

    @Test
    public void testBalancesInitialization() {
        assertEquals(0, balances.getOrDefault("address1", 0));
        assertEquals(0, balances.getOrDefault("address2", 0));
        assertEquals(0, balances.getOrDefault("address3", 0));
        assertEquals(0, balances.getOrDefault("address4", 0));
        assertEquals(0, balances.getOrDefault("address5", 0));
    }

    @Test
    public void testOutputsInitialization() {
        assertEquals(null, outputs.get("hash1:0"));
        assertEquals(null, outputs.get("hash2:1"));
        assertEquals(null, outputs.get("hash3:2"));
        assertEquals(null, outputs.get("hash4:3"));
        assertEquals(null, outputs.get("hash5:4"));
    }

    @Test
    public void testBalanceUpdate() {
        balances.put("address1", balances.getOrDefault("address1", 0) + 50);
        assertEquals(50, balances.get("address1"));
        balances.put("address1", balances.get("address1") - 20);
        assertEquals(30, balances.get("address1"));
        balances.put("address1", balances.get("address1") + 70);
        assertEquals(100, balances.get("address1"));
        balances.put("address1", balances.get("address1") - 10);
        assertEquals(90, balances.get("address1"));
        balances.put("address1", balances.get("address1") + 10);
        assertEquals(100, balances.get("address1"));
    }

    @Test
    public void testOutputUpdate() {
        outputs.put("hash1:0", "address1:100");
        assertEquals("address1:100", outputs.get("hash1:0"));
        outputs.put("hash1:0", "address1:50");
        assertEquals("address1:50", outputs.get("hash1:0"));
        outputs.put("hash2:1", "address2:200");
        assertEquals("address2:200", outputs.get("hash2:1"));
        outputs.put("hash3:2", "address3:150");
        assertEquals("address3:150", outputs.get("hash3:2"));
        outputs.put("hash4:3", "address4:300");
        assertEquals("address4:300", outputs.get("hash4:3"));
    }

    @Test
    public void testInvalidAddress() {
        assertEquals(0, balances.getOrDefault("invalid", 0));
        assertEquals(0, balances.getOrDefault("unknown", 0));
        assertEquals(0, balances.getOrDefault("nonexistent", 0));
        assertEquals(0, balances.getOrDefault("", 0));
        assertEquals(0, balances.getOrDefault(null, 0));
    }

    @Test
    public void testToHex() {
        assertEquals("030201", BlockChain.toHex(new byte[]{0x01, 0x02, 0x03}));
        assertEquals("00", BlockChain.toHex(new byte[]{0x00}));
        assertEquals("ffff", BlockChain.toHex(new byte[]{(byte) 0xFF, (byte) 0xFF}));
        assertEquals("c0b0a0", BlockChain.toHex(new byte[]{(byte) 0xA0, (byte) 0xB0, (byte) 0xC0}));
        assertEquals("010000", BlockChain.toHex(new byte[]{0x00, 0x00, 0x01}));
    }

    @Test
    public void testDoubleHash() throws NoSuchAlgorithmException {
        assertEquals(
            MessageDigest.getInstance("SHA-256").digest(
                MessageDigest.getInstance("SHA-256").digest("hello".getBytes())
            ),
            BlockChain.doubleHash("hello".getBytes())
        );
        assertEquals(
            MessageDigest.getInstance("SHA-256").digest(
                MessageDigest.getInstance("SHA-256").digest("".getBytes())
            ),
            BlockChain.doubleHash("".getBytes())
        );
        assertEquals(
            MessageDigest.getInstance("SHA-256").digest(
                MessageDigest.getInstance("SHA-256").digest("test".getBytes())
            ),
            BlockChain.doubleHash("test".getBytes())
        );
        assertEquals(
            MessageDigest.getInstance("SHA-256").digest(
                MessageDigest.getInstance("SHA-256").digest(new byte[]{0x00, 0x01, 0x02, 0x03})
            ),
            BlockChain.doubleHash(new byte[]{0x00, 0x01, 0x02, 0x03})
        );
        assertEquals(
            MessageDigest.getInstance("SHA-256").digest(
                MessageDigest.getInstance("SHA-256").digest("blockchain".getBytes())
            ),
            BlockChain.doubleHash("blockchain".getBytes())
        );
    }

    @Test
    public void testBase58() {
        assertEquals("11", BlockChain.base58(new byte[]{0x00}));
        assertEquals("111", BlockChain.base58(new byte[]{0x00, 0x00}));
        assertEquals("2", BlockChain.base58(new byte[]{0x01}));
        assertEquals("12", BlockChain.base58(new byte[]{0x00, 0x01}));
        assertEquals("5Q", BlockChain.base58(new byte[]{(byte) 0xFF}));
    }

    @Test
    public void testBlockChainInitialization() {
        byte[] data = new byte[100];
        BlockChain blockChain = new BlockChain(data);
        assertEquals(data, blockChain.getData());
        assertEquals(0, blockChain.getIndex());
        assertEquals(0, blockChain.getBlockCount());
        blockChain.setData(new byte[100]);
        assertEquals(new byte[100], blockChain.getData());
        blockChain.setIndex(10);
        assertEquals(10, blockChain.getIndex());
        blockChain.setBlockCount(1);
        assertEquals(1, blockChain.getBlockCount());
    }
}
