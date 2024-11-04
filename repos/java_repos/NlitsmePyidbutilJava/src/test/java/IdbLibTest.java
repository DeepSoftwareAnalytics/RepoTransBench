import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

public class IdbLibTest {

    @Test
    public void testFileSection() throws IOException {
        InputStream s = IdbLib.makeStringIO("0123456789abcdef".getBytes());
        IdbLib.FileSection fh = new IdbLib.FileSection(s, 3, 11);
        assertArrayEquals("345".getBytes(), fh.read(3));
        assertArrayEquals("6789a".getBytes(), fh.read(8));
        assertArrayEquals("".getBytes(), fh.read(8));

        fh.seek(-1, 2);
        assertArrayEquals("a".getBytes(), fh.read(8));
        fh.seek(3, 0);
        assertArrayEquals("67".getBytes(), fh.read(2));
        fh.seek(-2, 1);
        assertArrayEquals("67".getBytes(), fh.read(2));
        fh.seek(2, 1);
        assertArrayEquals("a".getBytes(), fh.read(2));

        fh.seek(8, 0);
        assertArrayEquals("".getBytes(), fh.read(1));
        assertThrows(IOException.class, () -> fh.seek(9, 0));
    }

    @Test
    public void testBinarySearch() {
        IdbLib.Object[] lst = {
            new IdbLib.Object(2),
            new IdbLib.Object(3),
            new IdbLib.Object(5),
            new IdbLib.Object(6)
        };
        assertEquals(-1, IdbLib.binarySearch(lst, 1));
        assertEquals(0, IdbLib.binarySearch(lst, 2));
        assertEquals(1, IdbLib.binarySearch(lst, 3));
        assertEquals(1, IdbLib.binarySearch(lst, 4));
        assertEquals(2, IdbLib.binarySearch(lst, 5));
        assertEquals(3, IdbLib.binarySearch(lst, 6));
        assertEquals(3, IdbLib.binarySearch(lst, 7));
    }

    @Test
    public void testEmptyList() {
        IdbLib.Object[] lst = {};
        assertEquals(-1, IdbLib.binarySearch(lst, 1));
    }

    @Test
    public void testOneElem() {
        IdbLib.Object[] lst = { new IdbLib.Object(1) };
        assertEquals(-1, IdbLib.binarySearch(lst, 0));
        assertEquals(0, IdbLib.binarySearch(lst, 1));
        assertEquals(0, IdbLib.binarySearch(lst, 2));
    }

    @Test
    public void testTwoElem() {
        IdbLib.Object[] lst = {
            new IdbLib.Object(1),
            new IdbLib.Object(3)
        };
        assertEquals(-1, IdbLib.binarySearch(lst, 0));
        assertEquals(0, IdbLib.binarySearch(lst, 1));
        assertEquals(0, IdbLib.binarySearch(lst, 2));
        assertEquals(1, IdbLib.binarySearch(lst, 3));
        assertEquals(1, IdbLib.binarySearch(lst, 4));
    }

    @Test
    public void testListSize() {
        for (int l = 3; l < 32; l++) {
            IdbLib.Object[] lst = new IdbLib.Object[l - 1];
            for (int i = 0; i < l - 1; i++) {
                lst[i] = new IdbLib.Object(i + 1);
            }
            assertEquals(-1, IdbLib.binarySearch(lst, 0));
            assertEquals(0, IdbLib.binarySearch(lst, 1));
            assertEquals(0, IdbLib.binarySearch(lst, 2));
            assertEquals(1, IdbLib.binarySearch(lst, 3));
            assertEquals(l - 3, IdbLib.binarySearch(lst, l - 1));
            assertEquals(l - 2, IdbLib.binarySearch(lst, l));
            assertEquals(l - 2, IdbLib.binarySearch(lst, l + 1));
            assertEquals(l - 2, IdbLib.binarySearch(lst, l + 2));
        }
    }
}

