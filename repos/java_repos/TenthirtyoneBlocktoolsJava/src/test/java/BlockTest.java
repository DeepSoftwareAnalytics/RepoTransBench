import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockTest {

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
        Block block = new Block(this.blockchain);
        assertNotNull(block);
        assertNotNull(block.getBlocksize());
        assertNotNull(block.continueParsing());
    }

    @Test
    public void test_continueParsing() throws IOException {
        Block block = new Block(this.blockchain);
        assertTrue(block.continueParsing());
    }

    @Test
    public void test_getBlocksize() throws IOException {
        Block block = new Block(this.blockchain);
        assertEquals(block.getBlocksize(), block.getBlocksize());
    }

    @Test
    public void test_hasLength() throws IOException {
        Block block = new Block(this.blockchain);
        assertTrue(block.hasLength(this.blockchain, 8));
    }

    @AfterEach
    public void tearDown() throws IOException {
        this.blockchain.close();
        new java.io.File("mock_blockchain").delete();
    }
}
