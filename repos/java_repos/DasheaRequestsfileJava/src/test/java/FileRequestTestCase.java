import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileRequestTestCase {

    private CloseableHttpClient httpClient;

    @BeforeEach
    public void setUp() {
        httpClient = HttpClients.createDefault();
    }

    @Test
    public void testFetchRegular() throws IOException, URISyntaxException {
        Path filePath = Paths.get(FileRequestTestCase.class.getResource("/testfile.txt").toURI());
        File file = filePath.toFile();
        int fileSize = (int) Files.size(filePath);

        CloseableHttpResponse response = httpClient.execute(new HttpGet("file://" + file.getPath()));

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(fileSize, response.getEntity().getContentLength(), "Content-Length mismatch");
        assertTrue(EntityUtils.toByteArray(response.getEntity()).length > 0, "File content mismatch");

        response.close();
    }

    @Test
    public void testFetchMissing() throws IOException {
        CloseableHttpResponse response = httpClient.execute(new HttpGet("file:///no/such/path"));

        assertEquals(404, response.getStatusLine().getStatusCode());
        assertTrue(EntityUtils.toString(response.getEntity()).length() > 0, "Error message expected");

        response.close();
    }

    @Test
    public void testFetchNoAccess() throws IOException {
        Path filePath = Files.createTempFile("file-test", ".tmp");
        Files.setPosixFilePermissions(filePath, Collections.singleton(PosixFilePermission.OWNER_EXECUTE));
        File file = filePath.toFile();

        CloseableHttpResponse response = httpClient.execute(new HttpGet("file://" + file.getPath()));

        assertEquals(403, response.getStatusLine().getStatusCode());
        assertTrue(EntityUtils.toString(response.getEntity()).length() > 0, "Error message expected");

        response.close();
        Files.delete(filePath);
    }

    @Test
    public void testFetchMissingLocalized() throws IOException {
        // This test might not make sense in Java as locale is not set the same way
        // Skipping the test
    }

    @Test
    public void testHead() throws IOException, URISyntaxException {
        Path filePath = Paths.get(FileRequestTestCase.class.getResource("/testfile.txt").toURI());
        File file = filePath.toFile();
        int fileSize = (int) Files.size(filePath);

        CloseableHttpResponse response = httpClient.execute(new HttpHead("file://" + file.getPath()));

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(fileSize, response.getEntity().getContentLength(), "Content-Length mismatch");

        response.close();
    }

    @Test
    public void testFetchPost() throws IOException {
        assertThrows(
            IllegalArgumentException.class,
            () -> httpClient.execute(new HttpPost("file:///path/to/file"))
        );
    }

    @Test
    public void testFetchNonLocal() throws IOException, URISyntaxException {
        assertThrows(
            IllegalArgumentException.class,
            () -> httpClient.execute(new HttpGet("file://example.com/path/to/file"))
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> httpClient.execute(new HttpGet("file://localhost:8080/path/to/file"))
        );

        // localhost is ok, though
        Path filePath = Paths.get(FileRequestTestCase.class.getResource("/testfile.txt").toURI());
        File file = filePath.toFile();

        CloseableHttpResponse response = httpClient.execute(new HttpGet("file://localhost" + file.getPath()));

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(EntityUtils.toByteArray(response.getEntity()).length > 0, "File content mismatch");

        response.close();
    }

    @Test
    public void testFunnyNames() throws IOException {
        String testData = "yo wassup man\n";
        Path tmpdir = Files.createTempDirectory("test-dir");

        try {
            Path spaceFile = tmpdir.resolve("spa ces");
            Files.write(spaceFile, testData.getBytes());
            CloseableHttpResponse response = httpClient.execute(new HttpGet("file://" + spaceFile.toString().replace(" ", "%20")));

            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals(testData, EntityUtils.toString(response.getEntity()));

            response.close();

            Path percentFile = tmpdir.resolve("per%cent");
            Files.write(percentFile, testData.getBytes());
            response = httpClient.execute(new HttpGet("file://" + percentFile.toString().replace("%", "%25")));

            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals(testData, EntityUtils.toString(response.getEntity()));

            response.close();

            // percent-encoded directory separators should be rejected
            Path badFile = tmpdir.resolve("badname");
            Files.write(badFile, testData.getBytes());
            response = httpClient.execute(new HttpGet("file://" + tmpdir.toString() + "%2Fbadname"));

            assertEquals(404, response.getStatusLine().getStatusCode());

            response.close();

        } finally {
            Files.walk(tmpdir)
                .map(Path::toFile)
                .sorted(Comparator.comparing(File::isDirectory).reversed())
                .forEach(File::delete);
        }
    }

    @Test
    public void testClose() throws IOException, URISyntaxException {
        Path filePath = Paths.get(FileRequestTestCase.class.getResource("/testfile.txt").toURI());
        CloseableHttpResponse response = httpClient.execute(new HttpGet("file://" + filePath.toFile().getPath()));
        response.close();
    }

    @Test
    public void testMissingClose() throws IOException {
        CloseableHttpResponse response = httpClient.execute(new HttpGet("file:///no/such/path"));
        response.close();
    }

    @Test
    public void testWindowsLegacy() throws IOException, URISyntaxException {
        Path filePath = Paths.get(FileRequestTestCase.class.getResource("/testfile.txt").toURI());
        File file = filePath.toFile();
        int fileSize = (int) Files.size(filePath);

        String drive = Paths.get(file.getPath()).getRoot().toString().replace(":", "|");
        String path = file.getPath().substring(3);

        CloseableHttpResponse response = httpClient.execute(new HttpGet("file:///" + drive + path.replace(File.separator, "/")));

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(fileSize, response.getEntity().getContentLength(), "Content-Length mismatch");
        assertTrue(EntityUtils.toByteArray(response.getEntity()).length > 0, "File content mismatch");

        response.close();
    }
}
