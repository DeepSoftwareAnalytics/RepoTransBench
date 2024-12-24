import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TxOutputTest {

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
        txOutput txOutput = new txOutput(this.blockchain);
        assertNotNull(txOutput.value);
        assertNotNull(txOutput.scriptLen);
        assertNotNull(txOutput.pubkey);
    }

    @AfterEach
    public void tearDown() throws IOException {
        this.blockchain.close();
        new java.io.File("mock_blockchain").delete();
    }
}
