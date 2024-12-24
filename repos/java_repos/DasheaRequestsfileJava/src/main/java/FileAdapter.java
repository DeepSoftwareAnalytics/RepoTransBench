import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class FileAdapter {
    public CloseableHttpResponse send(HttpRequestBase request) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // Check that the method makes sense. Only support GET and HEAD
        if (!(request instanceof HttpGet)) {
            throw new IllegalArgumentException("Invalid request method " + request.getMethod());
        }

        // Parse the URL
        String url = request.getURI().toString();
        java.net.URL urlObj = new java.net.URL(url);
        String path = urlObj.getPath();

        // Validate the URL
        if (urlObj.getHost() != null && !urlObj.getHost().isEmpty() && !urlObj.getHost().equals("localhost")) {
            throw new IllegalArgumentException("file: URLs with hostname components are not permitted");
        }

        // Convert URL path to file system path
        File file = Paths.get(path).toFile();

        // Handle non-existent files
        if (!file.exists()) {
            CloseableHttpResponse response = httpClient.execute(new HttpGet("file://" + file.getPath()));
            response.setStatusCode(404); // Not Found
            return response;
        }

        // Handle the file response
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            CloseableHttpResponse response = httpClient.execute(new HttpGet("file://" + file.getPath()));
            response.setStatusCode(200); // OK
            response.setEntity(new InputStreamEntity(fileInputStream));
            return response;
        } catch (IOException e) {
            CloseableHttpResponse response = httpClient.execute(new HttpGet("file://" + file.getPath()));
            if (e instanceof java.nio.file.AccessDeniedException) {
                response.setStatusCode(403); // Forbidden
            } else {
                response.setStatusCode(400); // Bad Request
            }
            return response;
        }
    }
}
