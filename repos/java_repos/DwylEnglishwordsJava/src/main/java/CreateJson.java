import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.*;

public class CreateJson {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java CreateJson <filename>");
            System.exit(1);
        }

        String filename = args[0];
        Map<String, String> jsonWords = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonWords.put(line.trim(), "1");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String jsonString = new JSONObject(jsonWords).toString(4);
        System.out.println(jsonString);
    }
}
