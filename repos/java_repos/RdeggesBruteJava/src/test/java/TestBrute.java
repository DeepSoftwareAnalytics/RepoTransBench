import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.util.Iterator;

public class TestBrute {

    @Test
    public void testBruteLengthDefault() {
        String lastStr = "";
        Iterator<String> iterator = Brute.brute().iterator();
        while (iterator.hasNext()) {
            lastStr = iterator.next();
        }
        assertEquals(3, lastStr.length());
    }

    @Test
    public void testBruteReturnsGenerator() {
        assertTrue(Brute.brute() instanceof Iterable);
    }

    @Test
    public void testLettersNumbersSymbolsWhitespaceDefault() {
        boolean letters = false;
        boolean numbers = false;
        boolean symbols = false;
        boolean whitespace = true;

        Iterator<String> iterator = Brute.brute().iterator();
        while (iterator.hasNext()) {
            String pw = iterator.next();
            if (pw.contains("a")) {
                letters = true;
            } else if (pw.contains("1")) {
                numbers = true;
            } else if (pw.contains("!")) {
                symbols = true;
            } else if (pw.contains(" ")) {
                whitespace = false;
            }
        }

        assertTrue(letters);
        assertTrue(numbers);
        assertTrue(symbols);
        assertTrue(whitespace);
    }

    @Test
    public void testDisableLetters() {
        boolean letters = true;

        Iterator<String> iterator = Brute.brute(false, true, true, false).iterator();
        while (iterator.hasNext()) {
            String pw = iterator.next();
            if (pw.contains("a")) {
                letters = false;
            }
        }

        assertTrue(letters);
    }

    @Test
    public void testDisableNumbers() {
        boolean numbers = true;

        Iterator<String> iterator = Brute.brute(true, false, true, false).iterator();
        while (iterator.hasNext()) {
            String pw = iterator.next();
            if (pw.contains("1")) {
                numbers = false;
            }
        }

        assertTrue(numbers);
    }

    @Test
    public void testDisableSymbols() {
        boolean symbols = true;

        Iterator<String> iterator = Brute.brute(true, true, false, false).iterator();
        while (iterator.hasNext()) {
            String pw = iterator.next();
            if (pw.contains("!")) {
                symbols = false;
            }
        }

        assertTrue(symbols);
    }

    @Test
    public void testEnableSpaces() {
        boolean spaces = false;

        Iterator<String> iterator = Brute.brute(true, true, true, true).iterator();
        while (iterator.hasNext()) {
            String pw = iterator.next();
            if (pw.contains(" ")) {
                spaces = true;
            }
        }

        assertTrue(spaces);
    }

    @Test
    public void testRamp() {
        Iterator<String> iterator = Brute.brute(3, false).iterator();
        while (iterator.hasNext()) {
            String pw = iterator.next();
            assertEquals(3, pw.length());
        }
    }

    @Test
    public void testRampStartLength() {
        Iterator<String> iterator = Brute.brute(2, 3, true).iterator();
        while (iterator.hasNext()) {
            String pw = iterator.next();
            assertTrue(pw.length() >= 2);
        }
    }

    @Test
    public void testRampBadStartLength() {
        Iterator<String> iterator = Brute.brute(0, 3, true).iterator();
        while (iterator.hasNext()) {
            String pw = iterator.next();
            assertTrue(pw.length() >= 1);
        }

        iterator = Brute.brute(4, 3, true).iterator();
        while (iterator.hasNext()) {
            String pw = iterator.next();
            assertFalse(pw.length() >= 4);
        }
    }

}
