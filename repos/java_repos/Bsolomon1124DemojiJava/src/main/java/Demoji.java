import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Demoji {
    private static final String URL = "https://unicode.org/Public/emoji/13.1/emoji-test.txt";
    private static final Path CACHEPATH = Paths.get("src/main/resources/demoji/codes.json");
    private static LocalDateTime lastDownloadedTimestamp = LocalDateTime.of(2021, 7, 18, 19, 57, 25, 20304);
    private static Pattern emojiPattern = null;
    private static Map<String, String> codeToDesc = new HashMap<>();

    private static void setEmojiPattern() {
        if (emojiPattern == null) {
            loadCodesFromFile();
            StringBuilder patternBuilder = new StringBuilder();
            for (String code : codeToDesc.keySet()) {
                patternBuilder.append(Pattern.quote(code)).append("|");
            }
            emojiPattern = Pattern.compile(patternBuilder.substring(0, patternBuilder.length() - 1));
        }
    }

    private static void loadCodesFromFile() {
        try (BufferedReader reader = Files.newBufferedReader(CACHEPATH)) {
            JSONObject json = new JSONObject(new String(Files.readAllBytes(CACHEPATH)));
            for (String key : json.keySet()) {
                codeToDesc.put(key, json.getString(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> findall(String str) {
        setEmojiPattern();
        Map<String, String> result = new HashMap<>();
        Matcher matcher = emojiPattern.matcher(str);
        while (matcher.find()) {
            result.put(matcher.group(), codeToDesc.get(matcher.group()));
        }
        return result;
    }

    public static List<String> findallList(String str, boolean desc) {
        setEmojiPattern();
        List<String> result = new ArrayList<>();
        Matcher matcher = emojiPattern.matcher(str);
        while (matcher.find()) {
            if (desc) {
                result.add(codeToDesc.get(matcher.group()));
            } else {
                result.add(matcher.group());
            }
        }
        return result;
    }

    public static String replace(String str) {
        setEmojiPattern();
        Matcher matcher = emojiPattern.matcher(str);
        return matcher.replaceAll("");
    }

    public static String replace(String str, String repl) {
        setEmojiPattern();
        Matcher matcher = emojiPattern.matcher(str);
        return matcher.replaceAll(repl);
    }

    public static String replaceWithDesc(String str, String sep) {
        setEmojiPattern();
        String result = str;
        for (Map.Entry<String, String> entry : findall(str).entrySet()) {
            result = result.replace(entry.getKey(), sep + entry.getValue() + sep);
        }
        return result;
    }

    public static LocalDateTime lastDownloadedTimestamp() {
        return lastDownloadedTimestamp;
    }

    public static void downloadCodes() {
        StringBuilder data = new StringBuilder();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(URL))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String[] lines = response.body().split("\n");
            for (String line : lines) {
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                String[] parts = line.split(";");
                if (parts.length < 2)
                    continue;
                String[] descParts = parts[1].split("#");
                if (descParts.length < 2)
                    continue;
                String desc = descParts[1].substring(1).trim();
                String code = parts[0].trim();
                if (code.contains("..")) {
                    String[] range = code.split("\\.\\.");
                    int start = Integer.parseInt(range[0], 16);
                    int end = Integer.parseInt(range[1], 16);
                    for (int i = start; i <= end; i++) {
                        data.append(String.format("\\u%s", Integer.toHexString(i)));
                    }
                } else {
                    int intCode = Integer.parseInt(code, 16);
                    data.append(String.format("\\u%s", Integer.toHexString(intCode)));
                }
                data.append(",").append(desc).append("\n");
            }
            Files.writeString(CACHEPATH, data.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateLastDownloadedTimestamp();
    }

    private static void updateLastDownloadedTimestamp() {
        lastDownloadedTimestamp = LocalDateTime.now(ZoneId.of("UTC"));
    }

    public static void main(String[] args) {
        downloadCodes();
    }
}
