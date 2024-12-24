import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockToolsTest {

    private FileInputStream blockchain;

    @BeforeEach
    public void setUp() throws IOException {
        try (FileOutputStream fos = new FileOutputStream("mock_blockchain")) {
            fos.write(new byte[100]);  // Write 100 bytes of zero for mocking
        }
        this.blockchain = new FileInputStream("mock_blockchain");
    }

    @Test
    public void test_uint1() throws IOException {
        this.blockchain.getChannel().position(0);
        assertEquals(0, BlockTools.uint1(this.blockchain));
    }

    @Test
    public void test_uint2() throws IOException {
        this.blockchain.getChannel().position(0);
        assertEquals(0, BlockTools.uint2(this.blockchain));
    }

    @Test
    public void test_uint4() throws IOException {
        this.blockchain.getChannel().position(0);
        assertEquals(0, BlockTools.uint4(this.blockchain));
    }

    @Test
    public void test_uint8() throws IOException {
        assertEquals(0L, BlockTools.uint8(this.blockchain));
    }

    @Test
    public void test_hash32() throws IOException {
        this.blockchain.getChannel().position(0);
        byte[] hash32 = BlockTools.hash32(this.blockchain);
        byte[] expected = new byte[32];
        assertEquals(expected.length, hash32.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], hash32[i]);
        }
    }

    @Test
    public void test_varint() throws IOException {
        this.blockchain.getChannel().position(0);
        assertEquals(0L, BlockTools.varint(this.blockchain));
    }

    @Test
    public void test_hashStr() {
        byte[] bytes = new byte[32];
        assertEquals("00".repeat(32), BlockTools.hashStr(bytes));
    }

    @AfterEach
    public void tearDown() throws IOException {
        this.blockchain.close();
        new java.io.File("mock_blockchain").delete();
    }
}
