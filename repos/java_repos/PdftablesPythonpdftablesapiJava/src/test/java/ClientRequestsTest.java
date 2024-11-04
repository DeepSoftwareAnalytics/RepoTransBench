import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.mockito.MockedStatic;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.*;

class ClientRequestsTest {

    @Test
    void testSuccessfulConversion() throws Exception {
        try (MockedStatic<HttpClients> mockedHttpClients = Mockito.mockStatic(HttpClients.class)) {

            CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
            Mockito.when(mockResponse.getStatusLine()).thenReturn(Mockito.mock(org.apache.http.StatusLine.class));
            Mockito.when(mockResponse.getStatusLine().getStatusCode()).thenReturn(200);
            Mockito.when(mockResponse.getEntity()).thenReturn(Mockito.mock(HttpEntity.class));

            CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
            Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockResponse);

            mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockHttpClient);

            Client client = new Client("fake_key");

            File pdfFile = File.createTempFile("test", ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                fos.write("Hello world".getBytes());
            }

            File xlsxFile = File.createTempFile("test", ".xlsx");
            client.convert(pdfFile.getAbsolutePath(), xlsxFile.getAbsolutePath());

            Mockito.verify(mockResponse.getEntity()).writeTo(Mockito.any(OutputStream.class));
        }
    }

    @Test
    void testSuccessfulConversionBytes() throws Exception {
        try (MockedStatic<HttpClients> mockedHttpClients = Mockito.mockStatic(HttpClients.class)) {
    
            CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
            Mockito.when(mockResponse.getStatusLine()).thenReturn(Mockito.mock(org.apache.http.StatusLine.class));
            Mockito.when(mockResponse.getStatusLine().getStatusCode()).thenReturn(200);
            Mockito.when(mockResponse.getEntity()).thenReturn(Mockito.mock(HttpEntity.class));
    
            CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
            Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockResponse);
    
            mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockHttpClient);
    
            Client client = new Client("fake_key");
    
            File pdfFile = File.createTempFile("test", ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                fos.write("Hello world".getBytes());
            }
    
            // 直接返回字节数组并指定输出格式
            byte[] output = client.convert(pdfFile.getAbsolutePath(), null, "xlsx-multiple");
    
            Mockito.verify(mockResponse.getEntity()).writeTo(Mockito.any(OutputStream.class));
        }
    }
    
    @Test
    void testSuccessfulConversionString() throws Exception {
        try (MockedStatic<HttpClients> mockedHttpClients = Mockito.mockStatic(HttpClients.class)) {

            CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
            Mockito.when(mockResponse.getStatusLine()).thenReturn(Mockito.mock(org.apache.http.StatusLine.class));
            Mockito.when(mockResponse.getStatusLine().getStatusCode()).thenReturn(200);
            HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
            Mockito.when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream("csv output".getBytes()));
            Mockito.when(mockResponse.getEntity()).thenReturn(mockEntity);

            CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
            Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockResponse);

            mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockHttpClient);

            Client client = new Client("fake_key");

            File pdfFile = File.createTempFile("test", ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                fos.write("Hello world".getBytes());
            }

            byte[] outputBytes = client.convert(pdfFile.getAbsolutePath(), null, "csv");
            String output = new String(outputBytes);

            assertEquals("csv output", output);  // Correctly handle byte array to String
        }
    }

    @Test
    void testMissingApiKey() throws Exception {
        try (MockedStatic<HttpClients> mockedHttpClients = Mockito.mockStatic(HttpClients.class)) {

            CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
            Mockito.when(mockResponse.getStatusLine()).thenReturn(Mockito.mock(org.apache.http.StatusLine.class));
            Mockito.when(mockResponse.getStatusLine().getStatusCode()).thenReturn(401); // Unauthorized

            CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
            Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockResponse);

            mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockHttpClient);

            Client client = new Client("");

            File pdfFile = File.createTempFile("test", ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                fos.write("Hello world".getBytes());
            }

            assertThrows(APIException.class, () -> {
                client.convert(pdfFile.getAbsolutePath(), "csv");
            });
        }
    }

    @Test
    void testRemainingConversions() throws Exception {
        try (MockedStatic<HttpClients> mockedHttpClients = Mockito.mockStatic(HttpClients.class)) {

            CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
            Mockito.when(mockResponse.getStatusLine()).thenReturn(Mockito.mock(org.apache.http.StatusLine.class));
            Mockito.when(mockResponse.getStatusLine().getStatusCode()).thenReturn(200);

            CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
            Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockResponse);

            mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockHttpClient);

            Client client = new Client("fake_key");
            int remaining = client.remaining();

            assertEquals(8584, remaining); // Mock the remaining count to 8584
        }
    }

    @Test
    void testResponseUnknownFileFormat() throws Exception {
        try (MockedStatic<HttpClients> mockedHttpClients = Mockito.mockStatic(HttpClients.class)) {

            CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
            Mockito.when(mockResponse.getStatusLine()).thenReturn(Mockito.mock(org.apache.http.StatusLine.class));
            Mockito.when(mockResponse.getStatusLine().getStatusCode()).thenReturn(403); // Unknown file format

            CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
            Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockResponse);

            mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockHttpClient);

            Client client = new Client("fake_key");

            File pngFile = File.createTempFile("test", ".png");
            try (FileOutputStream fos = new FileOutputStream(pngFile)) {
                fos.write("PNG content".getBytes());
            }

            assertThrows(APIException.class, () -> {
                client.convert(pngFile.getAbsolutePath(), "csv");
            });
        }
    }

    // Helper method to consume the InputStream
    private byte[] consume(InputStream s) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = s.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
