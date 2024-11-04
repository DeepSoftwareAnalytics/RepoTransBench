import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.HashSet;

public class ReadEnglishDictionary {

    public static Set<String> loadWords() throws IOException {
        Set<String> validWords = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("words_alpha.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                validWords.add(line.trim());
            }
        }
        return validWords;
    }

    public static void main(String[] args) {
        try {
            Set<String> englishWords = loadWords();
            // demo print
            System.out.println(englishWords.contains("fate"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
