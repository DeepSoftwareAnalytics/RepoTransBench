import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;  // add this import
import java.util.ArrayList;  // add this import
import java.util.Collections;
import java.util.Arrays;
import java.util.Iterator;  // add this import
import java.util.NoSuchElementException;  // add this import

public class LookaheadTest {

    @Test
    public void testLookahead() {
        Lookahead<Object> l = new Lookahead<>(Collections.emptyList());
        assertTrue(l.isAtStart());
        assertTrue(l.isAtEnd());
        assertNull(l.peek());
        assertFalse(l.hasNext());

        l = new Lookahead<>(Collections.singleton("a"));
        assertTrue(l.isAtStart());
        assertFalse(l.isAtEnd());
        assertEquals('a', l.peek());
        assertEquals('a', l.next());
        assertFalse(l.isAtStart());
        assertTrue(l.isAtEnd());
        assertThrows(NoSuchElementException.class, l::next);

        l = new Lookahead<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        assertTrue(l.isAtStart());
        assertFalse(l.isAtEnd());
        assertEquals(0, l.peek());
        assertEquals(0, l.next());
        assertEquals(1, l.next());
        assertEquals(2, l.peek());
        assertFalse(l.isAtStart());
        assertFalse(l.isAtEnd());
        assertEquals(Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9), toList(l));
        assertTrue(l.isAtEnd());
    }

    private <T> List<T> toList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }
}

