import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class SSEClient implements Iterable<Event>, AutoCloseable { // Implement AutoCloseable

    private static final Logger logger = LoggerFactory.getLogger(SSEClient.class);

    private final BufferedReader reader;
    private String lastEventId;

    public SSEClient(InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    @Override
    public Iterator<Event> iterator() {
        return new Iterator<Event>() {
            @Override
            public boolean hasNext() {
                try {
                    return reader.ready();
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            public Event next() {
                try {
                    return readEvent();
                } catch (IOException e) {
                    throw new RuntimeException("Error reading event", e);
                }
            }
        };
    }

    private Event readEvent() throws IOException {
        Event event = new Event();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                break;  // End of event
            }

            if (line.startsWith(":")) {
                continue; // Comment line
            }

            String[] parts = line.split(":", 2);
            String field = parts[0];
            String value = (parts.length > 1) ? parts[1].trim() : "";

            switch (field) {
                case "id":
                    event.setId(value);
                    break;
                case "event":
                    event.setEvent(value);
                    break;
                case "data":
                    event.setData(event.getData() + value + "\n");
                    break;
                case "retry":
                    event.setRetry(Integer.parseInt(value));
                    break;
            }
        }

        if (event.getData().endsWith("\n")) {
            event.setData(event.getData().substring(0, event.getData().length() - 1));
        }

        if (event.getEvent().isEmpty()) {
            event.setEvent("message");
        }

        return event;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}

