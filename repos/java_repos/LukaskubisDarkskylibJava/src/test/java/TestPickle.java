import org.junit.jupiter.api.*;

import org.mockserver.client.MockServerClient;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.junit.jupiter.api.Assertions.*;

public class TestPickle {

    @BeforeAll
    public static void setUpClass() throws IOException {
        // Set up mock response
        MockServerClient mockServerClient = new MockServerClient("127.0.0.1", 1080);
        mockServerClient
            .when(request().withMethod("GET").withPath("/forecast/test_key/-77.843906,166.686520"))
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(Files.readString(Paths.get("src/test/resources/response.json"))) // Adjusted path
            );
    }

    @AfterAll
    public static void tearDownClass() throws IOException {
        // Clean up pickled files
        Files.walk(Paths.get("."))
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".pickle"))
            .map(java.nio.file.Path::toFile)
            .forEach(File::delete);
    }

    @Test
    public void testPickle() throws IOException, ClassNotFoundException {
        double latitude = -77.843906;
        double longitude = 166.686520;

        // Mocking request via MockServer, thus doesn't hit the API
        Forecast forecast = DarkSky.forecast("test_key", latitude, longitude);

        // Ensure the right data is retrieved via mock
        assertEquals(-23.58, forecast.getAttribute("currently.temperature"));

        // Ensure pickling by actually pickling
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./forecast.pickle"))) {
            out.writeObject(forecast);
        }

        // Check that the file exists
        assertTrue(Files.exists(Paths.get("./forecast.pickle")));
    }

    @Test
    public void testUnpickle() throws IOException, ClassNotFoundException {
        // Check that the previous test, which writes out the pickle, succeeded
        assertTrue(Files.exists(Paths.get("./forecast.pickle")));

        // Load the pickle file
        Forecast forecast;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("./forecast.pickle"))) {
            forecast = (Forecast) in.readObject();
        }

        // Make sure it loaded right
        assertNotNull(forecast);
        assertEquals(-23.58, forecast.getAttribute("currently.temperature"));
    }
}
