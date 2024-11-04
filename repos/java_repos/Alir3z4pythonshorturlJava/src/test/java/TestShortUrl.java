import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class TestShortUrl {

    private static final String TEST_DATA = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "data").toString();

    @BeforeAll
    public static void generateTestData() throws IOException {
        Map<Integer, String> result = new HashMap<>();

        for (int i = 0; i < 1000; i++) {
            String value = ShortUrl.encodeUrl(i);
            result.put(i, value);
        }

        Random random = new Random();
        while (result.size() < 10000) {
            int randomInt = random.nextInt(1000000);
            String value = ShortUrl.encodeUrl(randomInt);
            result.put(randomInt, value);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TEST_DATA, "key_values.txt"))) {
            for (Map.Entry<Integer, String> entry : result.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        }
    }

    private List<String> loadData(String filename) throws IOException {
        Path path = Paths.get(TEST_DATA, filename);
        return Files.readAllLines(path);
    }

    @Test
    public void testCustomAlphabet() {
        ShortUrl.UrlEncoder encoder = new ShortUrl.UrlEncoder("ab");
        String url = encoder.encodeUrl(12);
        assertEquals("bbaaaaaaaaaaaaaaaaaaaa", url);
        int key = encoder.decodeUrl("bbaaaaaaaaaaaaaaaaaaaa");
        assertEquals(12, key);
    }

    @Test
    public void testTooShortAlphabet() {
        assertThrows(IllegalArgumentException.class, () -> new ShortUrl.UrlEncoder("aa"));
        assertThrows(IllegalArgumentException.class, () -> new ShortUrl.UrlEncoder("a"));
    }

    @Test
    public void testCalculatedValues() throws IOException {
        List<String> lines = loadData("key_values.txt");
        for (String line : lines) {
            String[] parts = line.split(":");
            int key = Integer.parseInt(parts[0]);
            String value = parts[1];
            String encodedValue = ShortUrl.encodeUrl(key);
            assertEquals(value, encodedValue);
            int decodedKey = ShortUrl.decodeUrl(encodedValue);
            assertEquals(decodedKey, key);
        }
    }
}
