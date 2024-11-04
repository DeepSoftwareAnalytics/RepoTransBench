import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.*;

public class Vocabulary {
    private static OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String GLOSBE_API_URL =
            "https://glosbe.com/gapi/translate?from=%s&dest=%s&format=json&pretty=true&phrase=%s";
    private static final String WORDNIK_API_URL =
            "http://api.wordnik.com/v4/word.json/%s/%s?api_key=1e940957819058fe3ec7c59d43c09504b400110db7faa0509";
    private static final String URBANDICT_API_URL =
            "http://api.urbandictionary.com/v0/%s?term=%s";
    private static final String BIGHUGELABS_API_URL =
            "http://words.bighugelabs.com/api/2/eb4e57bb2c34032da68dfeb3a0578b68/%s/json";

    public static void setClient(OkHttpClient client) {
        Vocabulary.client = client;
    }

    public static List<Map<String, Object>> meaning(String phrase, String sourceLang, String destLang) {
        String url = String.format(GLOSBE_API_URL, sourceLang, destLang, phrase);
        JsonNode jsonNode = fetchJson(url);
        if (jsonNode == null) return Collections.emptyList();

        List<Map<String, Object>> meanings = new ArrayList<>();
        for (JsonNode tucNode : jsonNode.path("tuc")) {
            if (tucNode.has("meanings")) {
                for (JsonNode meaningNode : tucNode.path("meanings")) {
                    Map<String, Object> meaning = new HashMap<>();
                    meaning.put("seq", meanings.size());
                    meaning.put("text", meaningNode.path("text").asText());
                    meanings.add(meaning);
                }
            }
        }
        return meanings;
    }

    // Similar methods for synonym, translate, antonym, partOfSpeech, usageExample, pronunciation, hyphenation...

    private static JsonNode fetchJson(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return mapper.readTree(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
