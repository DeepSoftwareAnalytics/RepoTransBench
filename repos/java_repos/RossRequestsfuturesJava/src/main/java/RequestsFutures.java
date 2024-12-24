package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestsFutures {
    private HttpClient client;
    private ExecutorService executor;

    public RequestsFutures() {
        this(Executors.newFixedThreadPool(8));
    }

    public RequestsFutures(ExecutorService executor) {
        this.client = HttpClient.newBuilder().executor(executor).build();
        this.executor = executor;
    }

    public CompletableFuture<HttpResponse<String>> get(String url) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> post(String url, String data) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    // Additional HTTP methods can be implemented similarly

    public void close() {
        executor.shutdown();
    }

    public static void main(String[] args) throws Exception {
        RequestsFutures session = new RequestsFutures();
        // Example usage
        CompletableFuture<HttpResponse<String>> future = session.get("https://httpbin.org/get");
        HttpResponse<String> response = future.get();
        System.out.println("response status: " + response.statusCode());
        System.out.println(response.body());

        // Clean up
        session.close();
    }
}
