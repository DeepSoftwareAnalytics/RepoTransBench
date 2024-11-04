import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BlockHeaderTest {

    private InputStream blockchain;

    @BeforeEach
    public void setUp() throws IOException {
        try (FileOutputStream fos = new FileOutputStream("mock_blockchain")) {
            fos.write(new byte[100]);  // Write 100 bytes of zero for mocking
        }
        this.blockchain = new FileInputStream("mock_blockchain");
    }

    @Test
    public void test_init() throws IOException {
        BlockHeader header = new BlockHeader(this.blockchain);
        assertNotNull(header.version);
        assertNotNull(header.previousHash);
        assertNotNull(header.merkleHash);
        assertNotNull(header.time);
        assertNotNull(header.bits);
        assertNotNull(header.nonce);
    }

    @Test
    public void test_toString() throws IOException {
        BlockHeader header = new BlockHeader(this.blockchain);
        header.toString();
    }

    @Test
    public void test_decodeTime() throws IOException {
        BlockHeader header = new BlockHeader(this.blockchain);
        String decodedTime = header.decodeTime(1234567890);
        assertEquals("2009-02-13T23:31:30Z (UTC)", decodedTime);
    }

    @AfterEach
    public void tearDown() throws IOException {
        this.blockchain.close();
        new java.io.File("mock_blockchain").delete();
    }
}
