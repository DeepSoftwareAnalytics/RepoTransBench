import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MockLoggingHandler extends Handler {
    private List<String> messages;

    public MockLoggingHandler() {
        messages = new ArrayList<>();
    }

    @Override
    public void publish(LogRecord record) {
        if (record != null && record.getMessage() != null) {
            messages.add(record.getMessage());
        }
    }

    @Override
    public void flush() {
        // No-op
    }

    @Override
    public void close() throws SecurityException {
        // No-op
    }

    public List<String> getMessages() {
        return messages;
    }
}

