import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestsProcessPoolTestCase {

    private static final String HTTPBIN = "https://nghttp2.org/httpbin/";
    private static HttpClient client;

    @BeforeAll
    public static void setUpClass() {
        client = HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(2))
                .build();
    }

    @Test
    public void testFuturesSession() throws Exception {
        _assertFuturesSession();
    }

    @Test
    public void testExceptionRaised() throws Exception {
        assertThrows(RuntimeException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                _assertFuturesSession();
            }
        });
    }

    private void _assertFuturesSession() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new java.net.URI(HTTPBIN + "get"))
                .build();
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = future.get();
        assertEquals(200, response.statusCode());

        request = HttpRequest.newBuilder()
                .uri(new java.net.URI(HTTPBIN + "status/404"))
                .build();
        future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        response = future.get();
        assertEquals(404, response.statusCode());

        future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    assertEquals(200, resp.statusCode());
                    try {
                        JsonNode data = new ObjectMapper().readTree(resp.body());
                    } catch (Exception e) {
                        fail(e.getMessage());
                    }
                    return resp;
                });

        final CompletableFuture<HttpResponse<String>> futureWithException = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    throw new RuntimeException("boom");
                });

        assertThrows(RuntimeException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                futureWithException.get();
            }
        });
    }

    @Test
    public void testContext() throws Exception {
        try (AutoCloseableContextHelper sess = new AutoCloseableContextHelper()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new java.net.URI(HTTPBIN + "get"))
                    .build();
            CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> response = future.get();
            assertEquals(200, response.statusCode());
        }
    }

    private class AutoCloseableContextHelper implements AutoCloseable {
        private boolean exitCalled = false;

        @Override
        public void close() throws Exception {
            this.exitCalled = true;
        }

        public boolean isExitCalled() {
            return exitCalled;
        }
    }
}
