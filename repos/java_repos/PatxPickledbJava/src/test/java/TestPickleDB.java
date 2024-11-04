import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;  // Import java.util.Set
import java.util.List;  // Import java.util.List

public class TestPickleDB {
    private static PickleDB db;

    @BeforeAll
    public static void setUpClass() {
        db = PickleDB.load("tests.db", false);
    }

    @BeforeEach
    public void setUp() {
        db.deldb();
    }

    @Test
    public void testLoad() {
        PickleDB x = PickleDB.load("x.db", false);
        assertNotNull(x);
    }

    @Test
    public void testSugarGet() {
        db.set("foo", "bar");
        assertEquals("bar", db.get("foo"));
    }

    @Test
    public void testSugarSet() {
        db.set("foo", "bar");
        assertEquals("bar", db.get("foo"));
    }

    @Test
    public void testSugarRem() {
        db.set("foo", "bar");
        db.rem("foo");
        assertFalse(db.exists("foo"));
    }

    @Test
    public void testSet() {
        db.set("key", "value");
        assertEquals("value", db.get("key"));
    }

    @Test
    public void testGetAll() {
        db.set("key1", "value1");
        db.set("key2", "value2");
        db.dcreate("dict1");
        db.lcreate("list1");
        Set<String> keys = db.getAll();
        Set<String> expectedKeys = Set.of("key1", "key2", "dict1", "list1");
        assertEquals(expectedKeys, keys);
    }

    @Test
    public void testGet() {
        db.set("key", "value");
        assertEquals("value", db.get("key"));
    }

    @Test
    public void testRem() {
        db.set("key", "value");
        db.rem("key");
        assertFalse(db.exists("key"));
    }

    @Test
    public void testAppend() {
        db.set("key", "value");
        db.append("key", "value");
        assertEquals("valuevalue", db.get("key"));
    }

    @Test
    public void testExists() {
        db.set("key", "value");
        assertTrue(db.exists("key"));
        db.rem("key");
    }

    @Test
    public void testNotExists() {
        db.set("key", "value");
        assertFalse(db.exists("not_key"));
        db.rem("key");
    }

    @Test
    public void testLexists() {
        db.lcreate("list");
        db.ladd("list", "value");
        assertTrue(db.lexists("list", "value"));
    }

    @Test
    public void testNotLexists() {
        db.lcreate("list");
        db.ladd("list", "value");
        assertFalse(db.lexists("list", "not_value"));
    }

    @Test
    public void testLrange() {
        db.lcreate("list");
        db.ladd("list", "one");
        db.ladd("list", "two");
        db.ladd("list", "three");
        db.ladd("list", "four");
        assertEquals(List.of("two", "three"), db.lrange("list", 1, 3));
    }

    @Test
    public void testDexists() {
        db.dcreate("dict");
        db.dadd("dict", "key", "value");
        assertTrue(db.dexists("dict", "key"));
    }

    @Test
    public void testNotDexists() {
        db.dcreate("dict");
        db.dadd("dict", "key", "value");
        assertFalse(db.dexists("dict", "not_key"));
    }
    
}
