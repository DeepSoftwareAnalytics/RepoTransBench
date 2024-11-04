import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestReadEnglishDictionary {

    private String testWordsFile;

    @BeforeEach
    public void setUp() throws IOException {
        // Create a temporary words file for testing
        testWordsFile = "test_words_alpha.txt";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(testWordsFile))) {
            writer.write("apple\nbanana\ncherry\ndate\nelderberry\n");
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Remove the temporary words file after tests
        Files.deleteIfExists(Paths.get(testWordsFile));
    }

    @Test
    public void testLoadWords() throws IOException {
        // Mock the open function to use the test file
        InputStream originalInputStream = System.in;
        System.setIn(new FileInputStream(testWordsFile));

        Set<String> words = ReadEnglishDictionary.loadWords();
        assertTrue(words.contains("apple"));
        assertTrue(words.contains("banana"));
        assertTrue(words.contains("cherry"));
        assertTrue(words.contains("date"));
        assertTrue(words.contains("elderberry"));

        // Restore the original input stream
        System.setIn(originalInputStream);
    }

    @Test
    public void testLoadWordsType() throws IOException {
        // Mock the open function to use the test file
        InputStream originalInputStream = System.in;
        System.setIn(new FileInputStream(testWordsFile));

        Set<String> words = ReadEnglishDictionary.loadWords();
        assertTrue(words instanceof Set);

        // Restore the original input stream
        System.setIn(originalInputStream);
    }

    @Test
    public void testLoadWordsEmptyFile() throws IOException {
        String emptyFile = "empty_words_alpha.txt";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(emptyFile))) {
            writer.write("");
        }

        // Mock the open function to use the empty file
        InputStream originalInputStream = System.in;
        System.setIn(new FileInputStream(emptyFile));

        Set<String> words = ReadEnglishDictionary.loadWords();
        assertEquals(0, words.size());

        // Restore the original input stream
        System.setIn(originalInputStream);
        Files.deleteIfExists(Paths.get(emptyFile));
    }

    @Test
    public void testLoadWordsWithDuplicates() throws IOException {
        String duplicateFile = "duplicate_words_alpha.txt";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(duplicateFile))) {
            writer.write("apple\nbanana\napple\ncherry\nbanana\n");
        }

        // Mock the open function to use the duplicate file
        InputStream originalInputStream = System.in;
        System.setIn(new FileInputStream(duplicateFile));

        Set<String> words = ReadEnglishDictionary.loadWords();
        assertEquals(3, words.size());
        assertTrue(words.contains("apple"));
        assertTrue(words.contains("banana"));
        assertTrue(words.contains("cherry"));

        // Restore the original input stream
        System.setIn(originalInputStream);
        Files.deleteIfExists(Paths.get(duplicateFile));
    }

    @Test
    public void testLoadWordsWithSpecialCharacters() throws IOException {
        String specialCharFile = "special_char_words_alpha.txt";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(specialCharFile))) {
            writer.write("apple\nbanana\ncherry\n!@#$%\n12345\n");
        }

        // Mock the open function to use the special character file
        InputStream originalInputStream = System.in;
        System.setIn(new FileInputStream(specialCharFile));

        Set<String> words = ReadEnglishDictionary.loadWords();
        assertTrue(words.contains("apple"));
        assertTrue(words.contains("banana"));
        assertTrue(words.contains("cherry"));
        assertTrue(words.contains("!@#$%"));
        assertTrue(words.contains("12345"));

        // Restore the original input stream
        System.setIn(originalInputStream);
        Files.deleteIfExists(Paths.get(specialCharFile));
    }
}
