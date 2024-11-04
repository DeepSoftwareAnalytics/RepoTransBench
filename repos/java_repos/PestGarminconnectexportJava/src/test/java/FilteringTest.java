import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilteringTest {

    @Test
    public void testReadExclude() throws IOException {
        File tempFile = File.createTempFile("exclude", ".json");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("{\"ids\": [\"1001\", \"1002\", \"1010\", \"1003\"]}");
        }

        List<String> ids = Filtering.readExclude(tempFile.getPath());
        assertNotNull(ids);
        assertEquals(4, ids.size());
        assertTrue(ids.contains("1001"));
        assertTrue(ids.contains("1002"));
        assertTrue(ids.contains("1003"));
        assertTrue(ids.contains("1010"));

        tempFile.delete();
    }

    @Test
    public void testUpdateDownloadStats() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File tempFile = new File(tempDir, Filtering.DOWNLOADED_IDS_FILE_NAME);
        if (tempFile.exists()) {
            tempFile.delete();
        }

        Filtering.updateDownloadStats("1000", tempDir.getPath());
        List<String> ids = Filtering.readExclude(tempFile.getPath());
        assertNotNull(ids);
        assertEquals(1, ids.size());
        assertEquals("1000", ids.get(0));

        Filtering.updateDownloadStats("1010", tempDir.getPath());
        ids = Filtering.readExclude(tempFile.getPath());
        assertNotNull(ids);
        assertEquals(2, ids.size());
        assertEquals("1000", ids.get(0));
        assertEquals("1010", ids.get(1));

        tempFile.delete();
    }
}
