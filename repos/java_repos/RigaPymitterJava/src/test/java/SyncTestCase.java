import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.function.Consumer;

class SyncTestCase {

    @Test
    void testCallbackUsage() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.on("callback_usage", (arg) -> stack.add("callback_usage_" + arg));

        ee.emit("callback_usage", "foo");
        assertEquals(Arrays.asList("callback_usage_foo"), stack);
    }

    @Test
    void testDecoratorUsage() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.on("decorator_usage", (arg) -> stack.add("decorator_usage_" + arg));

        ee.emit("decorator_usage", "bar");
        assertEquals(Arrays.asList("decorator_usage_bar"), stack);
    }

    @Test
    void testTtlOn() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.on("ttl_on", (arg) -> stack.add("ttl_on_" + arg), 1);

        ee.emit("ttl_on", "foo");
        assertEquals(Arrays.asList("ttl_on_foo"), stack);

        ee.emit("ttl_on", "bar");
        assertEquals(Arrays.asList("ttl_on_foo"), stack);
    }

    @Test
    void testTtlOnce() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.once("ttl_once", (arg) -> stack.add("ttl_once_" + arg));

        ee.emit("ttl_once", "foo");
        assertEquals(Arrays.asList("ttl_once_foo"), stack);

        ee.emit("ttl_once", "bar");
        assertEquals(Arrays.asList("ttl_once_foo"), stack);
    }

    @Test
    void testWalkNodes() {
        EventEmitter ee = new EventEmitter();

        assertEquals(0, ee.getEventNames().length);

        ee.on("foo.bar.test", (arg) -> {});
        ee.on("foo.bar.test2", (arg) -> {});
        String[] expectedNodes = {"foo.bar.test", "foo.bar.test2"};
        assertArrayEquals(expectedNodes, ee.getEventNames());

        ee.offAll();
        assertEquals(0, ee.getEventNames().length);
    }

    @Test
    void testOnWildcards() {
        EventEmitter ee = new EventEmitter(true);
        List<Object> stack = new ArrayList<>();
        Object token = new Object();

        ee.on("on_all.*", (arg) -> stack.add(token));

        ee.emit("on_all.foo", null);
        assertTrue(stack.contains(token));
    }

    @Test
    void testOnAny() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.on("foo", (arg) -> stack.add("foo"));
        ee.onAny((arg) -> stack.add("bar"));

        ee.emit("foo", null);
        assertEquals(Arrays.asList("foo", "bar"), stack);
    }

    @Test
    void testOffAny() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        Consumer<Object> handler1 = (arg) -> stack.add("foo");

        ee.onAny(handler1);
        ee.emit("xyz", null);
        assertEquals(Arrays.asList("foo"), stack);

        stack.clear();
        ee.offAny(handler1);

        ee.emit("xyz", null);
        assertEquals(0, stack.size());
        assertEquals(0, ee.numListeners());
    }

    @Test
    void testOffAll() {
        EventEmitter ee = new EventEmitter();

        ee.onAny((arg) -> {});
        ee.on("foo", (arg) -> {});

        assertEquals(2, ee.numListeners());

        ee.offAll();
        assertEquals(0, ee.numListeners());
    }

    @Test
    void testListeners() {
        EventEmitter ee = new EventEmitter(true);

        Consumer<Object> h1 = (arg) -> {};
        Consumer<Object> h2 = (arg) -> {};
        Consumer<Object> h3 = (arg) -> {};
        Consumer<Object> h4 = (arg) -> {};
        Consumer<Object> h5 = (arg) -> {};

        ee.on("foo", h1);
        ee.on("foo", h2);
        ee.on("bar", h3);
        ee.once("baz", h4);
        ee.onAny(h5);

        assertEquals(1, ee.listenersAny().length);
        assertArrayEquals(new Consumer[]{h1, h2, h3, h4, h5}, ee.listenersAll());
        assertArrayEquals(new Consumer[]{h1, h2}, ee.listeners("foo"));
        assertArrayEquals(new Consumer[]{h3}, ee.listeners("bar"));
        assertArrayEquals(new Consumer[]{h4}, ee.listeners("baz"));
    }

    @Test
    void testEmitAll() {
        EventEmitter ee = new EventEmitter(true);
        List<String> stack = new ArrayList<>();

        ee.on("emit_all.foo", (arg) -> stack.add("emit_all.foo"));

        ee.emit("emit_all.*", null);
        assertEquals("emit_all.foo", stack.get(stack.size() - 1));
    }

    @Test
    void testOnReversePattern() {
        EventEmitter ee = new EventEmitter(true);
        List<String> stack = new ArrayList<>();

        ee.on("foo.bar", (arg) -> stack.add("on_foo_bar"));
        ee.on("foo.baz", (arg) -> stack.add("on_foo_baz"));
        ee.on("foo.bar.baz.test", (arg) -> stack.add("on_foo_bar_baz_test"));

        ee.emit("foo.ba?", null);
        assertEquals(Arrays.asList("on_foo_bar", "on_foo_baz"), stack);

        stack.clear();
        ee.emit("foo.bar.*.test", null);
        assertEquals(Arrays.asList("on_foo_bar_baz_test"), stack);
    }

    @Test
    void testDelimiter() {
        EventEmitter ee = new EventEmitter(true, ":");
        List<String> stack = new ArrayList<>();

        ee.on("delimiter:*", (arg) -> stack.add("delimiter"));

        ee.emit("delimiter:foo", null);
        assertEquals(Arrays.asList("delimiter"), stack);
    }

    @Test
    void testNew() {
        EventEmitter ee = new EventEmitter(true);
        List<Object[]> stack = new ArrayList<>();

        ee.on("new_listener", (arg) -> stack.add(new Object[]{arg, "event"}));

        Consumer<Object> newhandler = (arg) -> {};
        ee.on("new", newhandler);
        ee.onAny(newhandler);

        assertEquals(2, stack.size());
        assertEquals(newhandler, stack.get(0)[0]);
        assertEquals("new", stack.get(0)[1]);
        assertEquals(newhandler, stack.get(1)[0]);
        assertNull(stack.get(1)[1]);
    }

    @Test
    void testMax() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.on("max", (arg) -> stack.add("max_1"));
        ee.on("max", (arg) -> stack.add("max_2"));

        ee.emit("max", null);
        assertEquals(Arrays.asList("max_1", "max_2"), stack);
    }

    @Test
    void testTree() {
        EventEmitter ee = new EventEmitter();
        List<String> stack = new ArrayList<>();

        ee.on("max", (arg) -> stack.add("max_1"));
        ee.once("max", (arg) -> stack.add("max_2"));

        assertEquals(2, ee.numListeners());
        ee.emit("max", null);
        assertEquals(Arrays.asList("max_1", "max_2"), stack);

        stack.clear();
        ee.emit("max", null);
        assertEquals(Arrays.asList("max_1"), stack);

        assertEquals(1, ee.numListeners());
        ee.off("max", (arg) -> stack.add("max_1"));
        assertEquals(0, ee.numListeners());
    }
}
