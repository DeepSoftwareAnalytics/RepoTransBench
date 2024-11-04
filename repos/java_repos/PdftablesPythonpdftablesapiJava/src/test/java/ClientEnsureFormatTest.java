import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ClientEnsureFormatTest {

    @Test
    void testWrongFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            Client.ensureFormatExt("foo.csv", "txt");
        });

        Client client = new Client("key");
        assertThrows(IllegalArgumentException.class, () -> {
            client.convert("foo.pdf", null, "txt");
        });
    }

    @Test
    void testUnmodified() {
        assertEquals("foo.csv", Client.ensureFormatExt("foo.csv", "csv")[0]);
        assertEquals("csv", Client.ensureFormatExt("foo.csv", "csv")[1]);

        assertEquals("foo.xlsx", Client.ensureFormatExt("foo.xlsx", "xlsx-multiple")[0]);
        assertEquals("xlsx-multiple", Client.ensureFormatExt("foo.xlsx", "xlsx-multiple")[1]);

        assertEquals("foo.xml", Client.ensureFormatExt("foo.xml", "xml")[0]);
        assertEquals("xml", Client.ensureFormatExt("foo.xml", "xml")[1]);
    }

    @Test
    void testMissingFormat() {
        assertEquals("foo.xlsx", Client.ensureFormatExt("foo", null)[0]);
        assertEquals("xlsx-multiple", Client.ensureFormatExt("foo", null)[1]);

        assertEquals("foo.txt.xlsx", Client.ensureFormatExt("foo.txt", null)[0]);
        assertEquals("xlsx-multiple", Client.ensureFormatExt("foo.txt", null)[1]);
    }

    @Test
    void testMissingExt() {
        assertEquals("foo.csv", Client.ensureFormatExt("foo", "csv")[0]);
        assertEquals("csv", Client.ensureFormatExt("foo", "csv")[1]);
    }

    @Test
    void testIncorrectExt() {
        assertEquals("foo.txt.csv", Client.ensureFormatExt("foo.txt", "csv")[0]);
        assertEquals("csv", Client.ensureFormatExt("foo.txt", "csv")[1]);
    }

    @Test
    void testStdout() {
        assertEquals(null, Client.ensureFormatExt(null, null)[0]);
        assertEquals("xlsx-multiple", Client.ensureFormatExt(null, null)[1]);

        assertEquals(null, Client.ensureFormatExt(null, "csv")[0]);
        assertEquals("csv", Client.ensureFormatExt(null, "csv")[1]);
    }
}
