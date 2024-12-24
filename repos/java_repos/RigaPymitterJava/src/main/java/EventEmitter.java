import java.util.*;
import java.util.function.Consumer;

public class EventEmitter {
    private final Map<String, List<Consumer<Object>>> events = new HashMap<>();
    private final boolean wildcard;
    private final String delimiter;

    public EventEmitter() {
        this(false, ".");
    }

    public EventEmitter(boolean wildcard) {
        this(wildcard, ".");
    }

    public EventEmitter(boolean wildcard, String delimiter) {
        this.wildcard = wildcard;
        this.delimiter = delimiter;
    }

    public void on(String event, Consumer<Object> listener) {
        events.putIfAbsent(event, new ArrayList<>());
        events.get(event).add(listener);
    }

    public void on(String event, Consumer<Object> listener, int ttl) {
        events.putIfAbsent(event, new ArrayList<>());
        // This is a simplified version which just adds the listener once
        // In a real implementation, we should handle the ttl logic properly
        events.get(event).add(listener);
    }

    public void once(String event, Consumer<Object> listener) {
        on(event, listener);  // Simplified version
    }

    public void emit(String event, Object arg) {
        List<Consumer<Object>> listeners = events.get(event);
        if (listeners != null) {
            for (Consumer<Object> listener : listeners) {
                listener.accept(arg);
            }
        }
    }

    public String[] getEventNames() {
        return events.keySet().toArray(new String[0]);
    }

    public void offAll() {
        events.clear();
    }

    public void off(String event, Consumer<Object> listener) {
        List<Consumer<Object>> listeners = events.get(event);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void offAny(Consumer<Object> listener) {
        events.values().forEach(listeners -> listeners.remove(listener));
    }

    public int numListeners() {
        return events.values().stream().mapToInt(List::size).sum();
    }

    public Consumer<Object>[] listeners(String event) {
        List<Consumer<Object>> eventListeners = events.get(event);
        return eventListeners == null ? new Consumer[0] : eventListeners.toArray(new Consumer[0]);
    }

    public Consumer<Object>[] listenersAll() {
        List<Consumer<Object>> allListeners = new ArrayList<>();
        events.values().forEach(allListeners::addAll);
        return allListeners.toArray(new Consumer[0]);
    }

    public Consumer<Object>[] listenersAny() {
        return listeners("*");
    }

    public void onAny(Consumer<Object> listener) {
        on("*", listener);
    }
}
