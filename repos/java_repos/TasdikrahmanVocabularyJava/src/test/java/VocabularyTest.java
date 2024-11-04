import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class VocabularyTest {
    private final OkHttpClient mockedClient = Mockito.mock(OkHttpClient.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        Vocabulary.setClient(mockedClient);
    }

    @Test
    public void testMeaningFound() throws IOException {
        String jsonResponse = "{\"tuc\": [{\"meanings\": [{\"language\": \"en\",\"text\": \"the act of singing with closed lips\"}]}]}";
        ResponseBody responseBody = ResponseBody.create(jsonResponse, MediaType.get("application/json"));
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(responseBody)
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.meaning("humming", "en", "en");

        assertEquals(1, result.size());
        assertEquals("the act of singing with closed lips", result.get(0).get("text"));
    }

    @Test
    public void testMeaningNotFound() throws IOException {
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message("")
                .body(ResponseBody.create("", MediaType.get("application/json")))
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.meaning("humming", "en", "en");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testMeaningKeyError() throws IOException {
        String jsonResponse = "{\"result\": \"ok\", \"phrase\": \"humming\"}";
        ResponseBody responseBody = ResponseBody.create(jsonResponse, MediaType.get("application/json"));
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(responseBody)
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.meaning("humming", "en", "en");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testSynonymFound() throws IOException {
        String jsonResponse = "{\"tuc\": [{\"phrase\": {\"text\": \"get angry\",\"language\": \"en\"}}, {\"phrase\": {\"text\": \"mad\",\"language\": \"en\"}}]}";
        ResponseBody responseBody = ResponseBody.create(jsonResponse, MediaType.get("application/json"));
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(responseBody)
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.synonym("angry", "en");

        assertEquals(2, result.size());
        assertEquals("get angry", result.get(0).get("text"));
        assertEquals("mad", result.get(1).get("text"));
    }

    @Test
    public void testSynonymNotFound() throws IOException {
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message("")
                .body(ResponseBody.create("", MediaType.get("application/json")))
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.synonym("angry", "en");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testSynonymKeyError() throws IOException {
        String jsonResponse = "{\"result\": \"ok\", \"phrase\": \"angry\"}";
        ResponseBody responseBody = ResponseBody.create(jsonResponse, MediaType.get("application/json"));
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(responseBody)
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.synonym("angry", "en");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testTranslateFound() throws IOException {
        String jsonResponse = "{\"tuc\": [{\"phrase\": {\"text\": \"anglais\",\"language\": \"fr\"}}, {\"phrase\": {\"text\": \"germanique\",\"language\": \"fr\"}}]}";
        ResponseBody responseBody = ResponseBody.create(jsonResponse, MediaType.get("application/json"));
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(responseBody)
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.translate("english", "en", "fr");

        assertEquals(2, result.size());
        assertEquals("anglais", result.get(0).get("text"));
        assertEquals("germanique", result.get(1).get("text"));
    }

    @Test
    public void testTranslateNotFound() throws IOException {
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message("")
                .body(ResponseBody.create("", MediaType.get("application/json")))
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.translate("english", "en", "fr");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testAntonymFound() throws IOException {
        String jsonResponse = "{\"noun\": {\"ant\": [\"hate\", \"dislike\"]}, \"verb\": {\"ant\": [\"hate\", \"hater\"]}}";
        ResponseBody responseBody = ResponseBody.create(jsonResponse, MediaType.get("application/json"));
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(responseBody)
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.antonym("love", "en");

        assertEquals(3, result.size());
        assertEquals("hate", result.get(0).get("text"));
        assertEquals("dislike", result.get(1).get("text"));
        assertEquals("hater", result.get(2).get("text"));
    }

    @Test
    public void testAntonymNotFound() throws IOException {
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message("")
                .body(ResponseBody.create("", MediaType.get("application/json")))
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.antonym("love", "en");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testUsageExampleFound() throws IOException {
        String jsonResponse = "{\"list\": [{\"word\": \"hillock\", \"example\": \"I went to the top of the hillock to look around.\"}]}";
        ResponseBody responseBody = ResponseBody.create(jsonResponse, MediaType.get("application/json"));
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(responseBody)
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.usageExample("hillock", "en");

        assertEquals(1, result.size());
        assertEquals("I went to the top of the hillock to look around.", result.get(0).get("example"));
    }

    @Test
    public void testUsageExampleNotFound() throws IOException {
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message("")
                .body(ResponseBody.create("", MediaType.get("application/json")))
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.usageExample("hillock", "en");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testUsageExampleEmptyList() throws IOException {
        String jsonResponse = "{\"list\": [{\"definition\": \"a small mound or hill\", \"thumbs_up\": 0, \"word\": \"hillock\", \"example\": \"I went to the top of the hillock to look around.\", \"thumbs_down\": 3}]}";
        ResponseBody responseBody = ResponseBody.create(jsonResponse, MediaType.get("application/json"));
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(responseBody)
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.usageExample("hillock", "en");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testPronunciationFound() throws IOException {
        String jsonResponse = "[{\"rawType\": \"ahd-legacy\", \"seq\": 0, \"raw\": \"hip\"}, {\"rawType\": \"arpabet\", \"seq\": 0, \"raw\": \"HH IH2 P AH0 P AA1 T AH0 M AH0 S\"}]";
        ResponseBody responseBody = ResponseBody.create(jsonResponse, MediaType.get("application/json"));
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(responseBody)
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.pronunciation("hippopotamus", "en");

        assertEquals(2, result.size());
        assertEquals("hip", result.get(0).get("raw"));
        assertEquals("HH IH2 P AH0 P AA1 T AH0 M AH0 S", result.get(1).get("raw"));
    }

    @Test
    public void testPronunciationNotFound() throws IOException {
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message("")
                .body(ResponseBody.create("", MediaType.get("application/json")))
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.pronunciation("hippopotamus", "en");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testHyphenationFound() throws IOException {
        String jsonResponse = "[{\"seq\": 0, \"type\": \"secondary stress\", \"text\": \"hip\"}, {\"seq\": 1, \"text\": \"po\"}]";
        ResponseBody responseBody = ResponseBody.create(jsonResponse, MediaType.get("application/json"));
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(responseBody)
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.hyphenation("hippopotamus", "en");

        assertEquals(2, result.size());
        assertEquals("hip", result.get(0).get("text"));
        assertEquals("po", result.get(1).get("text"));
    }

    @Test
    public void testHyphenationNotFound() throws IOException {
        Response mockedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message("")
                .body(ResponseBody.create("", MediaType.get("application/json")))
                .build();

        Call mockedCall = Mockito.mock(Call.class);
        when(mockedClient.newCall(any(Request.class))).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);

        List<Map<String, Object>> result = Vocabulary.hyphenation("hippopotamus", "en");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testRespondAsDict1() throws IOException {
        String jsonData = "[{\"text\": \"hummus\", \"seq\": 0}]";
        Map<Integer, Map<String, Object>> expectedResult = Map.of(
                0, Map.of("text", "hummus")
        );

        Map<Integer, Map<String, Object>> result = Response.respondAsDict(mapper.readValue(jsonData, List.class));

        assertEquals(expectedResult, result);
    }

    @Test
    public void testRespondAsDict2() throws IOException {
        String jsonData = "[{\"text\": \"hummus\", \"seq\": 0}, {\"text\": \"hummusy\", \"seq\": 1}]";
        Map<Integer, Map<String, Object>> expectedResult = Map.of(
                0, Map.of("text", "hummus"),
                1, Map.of("text", "hummusy")
        );

        Map<Integer, Map<String, Object>> result = Response.respondAsDict(mapper.readValue(jsonData, List.class));

        assertEquals(expectedResult, result);
    }

    @Test
    public void testRespondAsDict3() throws IOException {
        String jsonData = "{\"text\": [\"hummus\"]}";
        Map<String, Object> expectedResult = Map.of("text", "hummus");

        Map<String, Object> result = Response.respondAsDict(mapper.readValue(jsonData, Map.class));

        assertEquals(expectedResult, result);
    }

    @Test
    public void testRespondAsList1() throws IOException {
        String jsonData = "[{\"text\": \"hummus\", \"seq\": 0}]";
        List<String> expectedResult = List.of("hummus");

        List<String> result = Response.respondAsList(mapper.readValue(jsonData, List.class));

        assertEquals(expectedResult, result);
    }

    @Test
    public void testRespondAsList2() throws IOException {
        String jsonData = "[{\"text\": \"hummus\", \"seq\": 0}, {\"text\": \"hummusy\", \"seq\": 1}]";
        List<String> expectedResult = List.of("hummus", "hummusy");

        List<String> result = Response.respondAsList(mapper.readValue(jsonData, List.class));

        assertEquals(expectedResult, result);
    }

    @Test
    public void testRespondAsList3() throws IOException {
        String jsonData = "{\"text\": [\"hummus\"]}";
        List<String> expectedResult = List.of("hummus");

        List<String> result = Response.respondAsList(mapper.readValue(jsonData, Map.class));

        assertEquals(expectedResult, result);
    }
}
