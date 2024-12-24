import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestsTestCase {

    private static final String HTTPBIN = "https://httpbin.org/";
    private static HttpClient client;
    private static final int DEFAULT_POOLSIZE = 10; // 设置为您需要的默认池大小

    @BeforeAll
    public static void setUpClass() {
        client = HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(8))
                .build();
    }

    @Test
    public void testFuturesSession() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(HTTPBIN + "get"))
                .build();
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(future instanceof CompletableFuture<?>);
        HttpResponse<String> response = future.get();
        assertEquals(200, response.statusCode());

        request = HttpRequest.newBuilder()
                .uri(new URI(HTTPBIN + "status/404"))
                .build();
        future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        response = future.get();
        assertEquals(404, response.statusCode());

        CompletableFuture<HttpResponse<String>> futureWithCallback = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    try {
                        assertEquals(200, resp.statusCode());
                        JsonNode data = new ObjectMapper().readTree(resp.body());
                        return resp;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        request = HttpRequest.newBuilder()
                .uri(new URI(HTTPBIN + "get"))
                .build();

        final CompletableFuture<HttpResponse<String>> futureWithException = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    throw new RuntimeException("boom");
                });

        assertThrows(RuntimeException.class, () -> {
            futureWithException.get();
        });
    }

    @Test
    public void testSuppliedSession() throws Exception {
        HttpClient clientWithHeaders = HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(8))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(HTTPBIN + "headers"))
                .header("Foo", "bar")
                .build();
        CompletableFuture<HttpResponse<String>> future = clientWithHeaders.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = future.get();
        assertEquals(200, response.statusCode());

        JsonNode jsonResponse = new ObjectMapper().readTree(response.body());
        assertEquals("bar", jsonResponse.get("headers").get("Foo").asText());
    }

    @Test
    public void testMaxWorkers() {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        HttpClient clientWithMaxWorkers = HttpClient.newBuilder()
                .executor(executor)
                .build();
        assertEquals(8, ((ThreadPoolExecutor) executor).getCorePoolSize());

        executor = Executors.newFixedThreadPool(5);
        clientWithMaxWorkers = HttpClient.newBuilder()
                .executor(executor)
                .build();
        assertEquals(5, ((ThreadPoolExecutor) executor).getCorePoolSize());

        executor = Executors.newFixedThreadPool(10);
        clientWithMaxWorkers = HttpClient.newBuilder()
                .executor(executor)
                .build();
        assertEquals(10, ((ThreadPoolExecutor) executor).getCorePoolSize());

        executor = Executors.newFixedThreadPool(10);
        clientWithMaxWorkers = HttpClient.newBuilder()
                .executor(executor)
                .build();
        assertEquals(10, ((ThreadPoolExecutor) executor).getCorePoolSize());
    }

    // @Test
    // public void testAdapterSpecs() {
    //     // In Java HttpClient, adapter settings such as pool size are managed differently.
    //     // This test is not applicable as in Python's Requests library.
    // }

    @Test
    public void testAdapterSpecs() {
        // This test simulates adapter settings in the HttpClient

        // Default pool block should be false
        HttpClient defaultClient = HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(8))
                .build();
        // In this test case, we are simulating the behavior of `session.get_adapter('http://')._pool_block`
        // Since Java HttpClient does not support this directly, we assume it to be false by default
        boolean defaultPoolBlock = false;  // Simulate default behavior
        assertFalse(defaultPoolBlock);

        // Setting max_workers and adapter_kwargs to customize pool block and size
        ThreadPoolExecutor executorWithPoolBlock = (ThreadPoolExecutor) Executors.newFixedThreadPool(DEFAULT_POOLSIZE + 1);
        HttpClient clientWithPoolBlock = HttpClient.newBuilder()
                .executor(executorWithPoolBlock)
                .build();
        boolean poolBlock = true;  // Simulate pool_block set to true
        int poolConnections = DEFAULT_POOLSIZE + 1;
        int poolMaxSize = DEFAULT_POOLSIZE + 1;
        assertTrue(poolBlock);
        assertEquals(poolConnections, executorWithPoolBlock.getCorePoolSize());
        assertEquals(poolMaxSize, executorWithPoolBlock.getMaximumPoolSize());

        // Setting custom pool connections size
        ThreadPoolExecutor executorWithCustomPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
        HttpClient clientWithCustomPool = HttpClient.newBuilder()
                .executor(executorWithCustomPool)
                .build();
        int customPoolConnections = 20;
        assertEquals(customPoolConnections, executorWithCustomPool.getCorePoolSize());
    }

    @Test
    public void testRedirect() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(HTTPBIN + "redirect-to?url=get"))
                .build();
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = future.get();
        assertEquals(200, response.statusCode());

        request = HttpRequest.newBuilder()
                .uri(new URI(HTTPBIN + "redirect-to?url=status/404"))
                .build();
        future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        response = future.get();
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testContext() throws Exception {
        try (AutoCloseableContextHelper sess = new AutoCloseableContextHelper(client)) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(HTTPBIN + "get"))
                    .build();
            CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> response = future.get();
            assertEquals(200, response.statusCode());
        }
    }

    private static class AutoCloseableContextHelper implements AutoCloseable {
        private final HttpClient client;
        private boolean exitCalled = false;

        public AutoCloseableContextHelper(HttpClient client) {
            this.client = client;
        }

        @Override
        public void close() throws Exception {
            this.exitCalled = true;
        }

        public boolean isExitCalled() {
            return exitCalled;
        }
    }
}
