import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SSEClientTest {

    private List<Event> parse(String content) {
        try (SSEClient sseClient = new SSEClient(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))) {
            return StreamSupport.stream(sseClient.spliterator(), false).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMultibyteCharacters() {
        String input = "id: 1\ndata: €豆腐\n\n";
        List<Event> result = parse(input);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("message", result.get(0).getEvent());
        assertEquals("€豆腐", result.get(0).getData());
    }

    @Test
    public void testParsesEmptyLinesWithMultibyteCharacters() {
        String input = "\n\n\n\nid: 1\ndata: 我現在都看實況不玩遊戲\n\n";
        List<Event> result = parse(input);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("message", result.get(0).getEvent());
        assertEquals("我現在都看實況不玩遊戲", result.get(0).getData());
    }

    @Test
    public void testOneOneLineMessageInOneChunk() {
        String input = "data: Hello\n\n";
        List<Event> result = parse(input);
        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0).getData());
    }

    @Test
    public void testOneOneLineMessageInTwoChunks() {
        String inputPart1 = "data: Hel";
        String inputPart2 = "lo\n\n";
        List<Event> result = parse(inputPart1 + inputPart2);
        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0).getData());
    }

    @Test
    public void testTwoOneLineMessagesInOneChunk() {
        String input = "data: Hello\n\ndata: World\n\n";
        List<Event> result = parse(input);
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).getData());
        assertEquals("World", result.get(1).getData());
    }

    @Test
    public void testOneTwoLineMessageInOneChunk() {
        String input = "data: Hello\ndata:World\n\n";
        List<Event> result = parse(input);
        assertEquals(1, result.size());
        assertEquals("Hello\nWorld", result.get(0).getData());
    }

    @Test
    public void testReallyChoppedUpUnicodeData() {
        String input = "data: Aslak\n\ndata: Hellesøy\n\n";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        List<Event> result = parse(new String(bytes, StandardCharsets.UTF_8));
        assertEquals(2, result.size());
        assertEquals("Aslak", result.get(0).getData());
        assertEquals("Hellesøy", result.get(1).getData());
    }

    @Test
    public void testAcceptsCRLFAsSeparator() {
        String input = "data: Aslak\r\n\r\ndata: Hellesøy\r\n\r\n";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        List<Event> result = parse(new String(bytes, StandardCharsets.UTF_8));
        assertEquals(2, result.size());
        assertEquals("Aslak", result.get(0).getData());
        assertEquals("Hellesøy", result.get(1).getData());
    }

    @Test
    public void testAcceptsCRAsSeparator() {
        String input = "data: Aslak\r\rdata: Hellesøy\r\r";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        List<Event> result = parse(new String(bytes, StandardCharsets.UTF_8));
        assertEquals(2, result.size());
        assertEquals("Aslak", result.get(0).getData());
        assertEquals("Hellesøy", result.get(1).getData());
    }

    @Test
    public void testDeliversMessageWithExplicitEvent() {
        String input = "event: greeting\ndata: Hello\n\n";
        List<Event> result = parse(input);
        assertEquals(1, result.size());
        assertEquals("greeting", result.get(0).getEvent());
        assertEquals("Hello", result.get(0).getData());
    }

    @Test
    public void testDeliversTwoMessagesWithSameExplicitEvent() {
        String input = "event: greeting\ndata: Hello\n\nevent: greeting\ndata: World\n\n";
        List<Event> result = parse(input);
        assertEquals(2, result.size());
        assertEquals("greeting", result.get(0).getEvent());
        assertEquals("Hello", result.get(0).getData());
        assertEquals("greeting", result.get(1).getEvent());
        assertEquals("World", result.get(1).getData());
    }

    @Test
    public void testDeliversTwoMessagesWithDifferentExplicitEvents() {
        String input = "event: greeting\ndata: Hello\n\nevent: salutation\ndata: World\n\n";
        List<Event> result = parse(input);
        assertEquals(2, result.size());
        assertEquals("greeting", result.get(0).getEvent());
        assertEquals("Hello", result.get(0).getData());
        assertEquals("salutation", result.get(1).getEvent());
        assertEquals("World", result.get(1).getData());
    }

    @Test
    public void testIgnoresComments() {
        String input = "data: Hello\n\n:nothing to see here\n\ndata: World\n\n";
        List<Event> result = parse(input);
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).getData());
        assertEquals("World", result.get(1).getData());
    }

    @Test
    public void testIgnoresEmptyComments() {
        String input = "data: Hello\n\n:\n\ndata: World\n\n";
        List<Event> result = parse(input);
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).getData());
        assertEquals("World", result.get(1).getData());
    }

    @Test
    public void testDoesNotIgnoreMultilineStrings() {
        String input = "data: line one\ndata:\ndata: line two\n\n";
        List<Event> result = parse(input);
        assertEquals(1, result.size());
        assertEquals("line one\n\nline two", result.get(0).getData());
    }

    @Test
    public void testDoesNotIgnoreMultilineStringsEvenInDataBeginning() {
        String input = "data:\ndata:line one\ndata: line two\n\n";
        List<Event> result = parse(input);
        assertEquals(1, result.size());
        assertEquals("\nline one\nline two", result.get(0).getData());
    }

    @Test
    public void testShouldRegardEmptyEventAsMessage() {
        String input = "event:\ndata: Hello\n\n";
        List<Event> result = parse(input);
        assertEquals(1, result.size());
        assertEquals("message", result.get(0).getEvent());
        assertEquals("Hello", result.get(0).getData());
    }

    @Test
    public void testShouldIgnoreMessageWithNoData() {
        String input = "event: greeting\n\n";
        List<Event> result = parse(input);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testPreservesWhitespaceAtEndOfLines() {
        String input = "event: greeting \ndata: Hello  \n\n";
        List<Event> result = parse(input);
        assertEquals(1, result.size());
        assertEquals("greeting ", result.get(0).getEvent());
        assertEquals("Hello  ", result.get(0).getData());
    }

    @Test
    public void testParsesRelativelyHugeMessagesEfficiently() {
        StringBuilder input = new StringBuilder("data: ");
        for (int i = 0; i < 10000; i++) {
            input.append('a');
        }
        input.append("\n\n");
        List<Event> result = parse(input.toString());
        assertEquals(1, result.size());
        assertEquals(input.toString().substring(6, 10006), result.get(0).getData());
    }

    @Test
    public void testID() {
        String input = "id: 90\ndata: Hello\n\n";
        List<Event> result = parse(input);
        assertEquals(1, result.size());
        assertEquals("90", result.get(0).getId());
        assertEquals("Hello", result.get(0).getData());
    }

    @Test
    public void testDoesNotSplitOnUniversalNewlines() {
        String input = "data: Hello\u000b\u000c\u001c\u001d\u001e\u0085\u2028\u2029\n\n";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        List<Event> result = parse(new String(bytes, StandardCharsets.UTF_8));
        assertEquals(1, result.size());
        assertEquals("Hello\u000b\u000c\u001c\u001d\u001e\u0085\u2028\u2029", result.get(0).getData());
    }

    @Test
    public void testEmptyLineAtStartOfChunk() {
        String inputPart1 = "event: test event\r\ndata: {\r\ndata:"
                + "     \"terribly_split\": \"json_objects in SSE\",";
        String inputPart2 = "\r\ndata:     \"which_should_probably\": "
                + "\"be on a single line\",\r\ndata:"
                + "     \"but_oh_well\": 1\r\ndata: }\r\n\r\n";
        List<Event> result = parse(inputPart1 + inputPart2);
        assertEquals(1, result.size());
        assertEquals("test event", result.get(0).getEvent());
        String expectedData = "{\n    \"terribly_split\": \"json_objects in SSE\",\n"
                + "    \"which_should_probably\": \"be on a single line\",\n"
                + "    \"but_oh_well\": 1\n}";
        assertEquals(expectedData, result.get(0).getData());
    }
}

