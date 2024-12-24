import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsyncTestCase {

    // Simulating async handling using threads and waits
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAsyncCallbackUsage() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.on("async_callback_usage", (Consumer<Object>) (arg) -> new Thread(() -> stack.add("async_callback_usage_" + arg)).start());

        ee.emit("async_callback_usage", "foo");
        sleep(100); // Wait for the thread to finish
        assertEquals(List.of("async_callback_usage_foo"), stack);
    }

    @Test
    void testAsyncDecoratorUsage() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.on("async_decorator_usage", (Consumer<Object>) (arg) -> new Thread(() -> stack.add("async_decorator_usage_" + arg)).start());

        ee.emit("async_decorator_usage", "bar");
        sleep(100); // Wait for the thread to finish
        assertEquals(List.of("async_decorator_usage_bar"), stack);
    }

    @Test
    void testAwaitAsyncCallbackUsage() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        Consumer<Object> handler = (arg) -> new Thread(() -> stack.add("await_async_callback_usage_" + arg)).start();
        ee.on("await_async_callback_usage", handler);

        ee.emit("await_async_callback_usage", "foo");
        assertEquals(0, stack.size());

        sleep(100); // Wait for the thread to finish
        assertEquals(List.of("await_async_callback_usage_foo"), stack);
    }

    @Test
    void testAwaitAsyncDecoratorUsage() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.on("await_async_decorator_usage", (Consumer<Object>) (arg) -> new Thread(() -> stack.add("await_async_decorator_usage_" + arg)).start());

        ee.emit("await_async_decorator_usage", "bar");
        assertEquals(0, stack.size());

        sleep(100); // Wait for the thread to finish
        assertEquals(List.of("await_async_decorator_usage_bar"), stack);
    }

    @Test
    void testEmitFuture() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.on("emit_future", (Consumer<Object>) (arg) -> new Thread(() -> stack.add("emit_future_" + arg)).start());

        Runnable test = () -> {
            ee.emit("emit_future", "bar");
            assertEquals(0, stack.size());
        };
        new Thread(test).start();

        sleep(100); // Let threads finish

        assertEquals(List.of("emit_future_bar"), stack);
    }

    @Test
    void testSupportsAsyncCallables() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        class EventHandler implements Consumer<Object> {
            @Override
            public void accept(Object arg) {
                stack.add((String) arg);
            }
        }

        ee.on("event", new EventHandler());

        ee.emit("event", "arg");

        sleep(100); // Wait for the thread to finish
        assertEquals(List.of("arg"), stack);
    }
}
