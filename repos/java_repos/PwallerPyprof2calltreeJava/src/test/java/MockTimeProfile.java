import java.util.ArrayList;
import java.util.List;

public class MockTimeProfile {
    private final List<Entry> entries = new ArrayList<>();
    private int mockTime = 0;
    private boolean running = false;

    public void start(String functionName) {
        this.mockTime = 0;
        this.running = true;
        Code code = new Code("ProfileCode", 1, functionName);
        entries.add(new Entry(code, 1, 0, 0, 0, new ArrayList<>()));
    }

    public void stop() {
        this.running = false;
    }

    public int timer() {
        if (!running) {
            throw new IllegalStateException("Timer is not running");
        }
        int now = mockTime;
        mockTime += 1000;
        return now;
    }

    public List<Entry> getEntries() {
        return entries;
    }
}
