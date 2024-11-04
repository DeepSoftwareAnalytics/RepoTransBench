import org.junit.jupiter.api.Test;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableTest {

    @Test
    public void testMarkdownEmptyTable() {
        Table table = new Table(Arrays.asList(new String[0][]));
        assertEquals("", table.markdown(null, null, false));
    }

    @Test
    public void testMarkdown() {
        List<String[]> cells = Arrays.asList(
                new String[]{"year", "make", "model", "description", "price"},
                new String[]{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
                new String[]{"1999", "Chevy", "Venture «Extended Edition»", "", "4900.00"},
                new String[]{"1996", "Jeep", "Grand Cherokee", "MUST SELL! air, moon roof, loaded", "4799.00"});
        Table table = new Table(cells);
        String expected = "| year | make  | model                      | description                       | price   |\n"
                + "| ---- | ----- | -------------------------- | --------------------------------- | ------- |\n"
                + "| 1997 | Ford  | E350                       | ac, abs, moon                     | 3000.00 |\n"
                + "| 1999 | Chevy | Venture «Extended Edition» |                                   | 4900.00 |\n"
                + "| 1996 | Jeep  | Grand Cherokee             | MUST SELL! air, moon roof, loaded | 4799.00 |";
        assertEquals(expected, table.markdown(null, null, false));
    }

    @Test
    public void testMarkdownWithAlignment() {
        List<String[]> cells = Arrays.asList(
                new String[]{"year", "make", "model", "description", "price"},
                new String[]{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
                new String[]{"1999", "Chevy", "Venture «Extended Edition»", "", "4900.00"},
                new String[]{"1996", "Jeep", "Grand Cherokee", "MUST SELL! air, moon roof, loaded", "4799.00"});
        Table table = new Table(cells);
        String expected = "| year | make  | model                      | description                       | price   |\n"
                + "| ---- | :---: | :------------------------: | --------------------------------- | ------: |\n"
                + "| 1997 | Ford  | E350                       | ac, abs, moon                     | 3000.00 |\n"
                + "| 1999 | Chevy | Venture «Extended Edition» |                                   | 4900.00 |\n"
                + "| 1996 | Jeep  | Grand Cherokee             | MUST SELL! air, moon roof, loaded | 4799.00 |";
        assertEquals(expected, table.markdown(Arrays.asList(1, 2), Arrays.asList(4), false));
    }

    @Test
    public void testMarkdownWithDefaultColumns() {
        List<String[]> cells = Arrays.asList(
                new String[]{"year", "make", "model", "description", "price"},
                new String[]{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
                new String[]{"1999", "Chevy", "Venture «Extended Edition»", "", "4900.00"},
                new String[]{"1996", "Jeep", "Grand Cherokee", "MUST SELL! air, moon roof, loaded", "4799.00"});
        Table table = new Table(cells);
        String expected = "| a    | b     | c                          | d                                 | e       |\n"
                + "| ---- | ----- | -------------------------- | --------------------------------- | ------- |\n"
                + "| year | make  | model                      | description                       | price   |\n"
                + "| 1997 | Ford  | E350                       | ac, abs, moon                     | 3000.00 |\n"
                + "| 1999 | Chevy | Venture «Extended Edition» |                                   | 4900.00 |\n"
                + "| 1996 | Jeep  | Grand Cherokee             | MUST SELL! air, moon roof, loaded | 4799.00 |";
        assertEquals(expected, table.markdown(null, null, true));
    }

    @Test
    public void testParseCsv() {
        String csv = "year,make,model,description,price\n"
                + "1997,Ford,E350,\"ac, abs, moon\",3000.00\n"
                + "1999,Chevy,\"Venture «Extended Edition»\",\"\",4900.00\n"
                + "1996,Jeep,Grand Cherokee,\"MUST SELL! air, moon roof, loaded\",4799.00";
        StringReader reader = new StringReader(csv);
        List<String[]> expectedCells = Arrays.asList(
                new String[]{"year", "make", "model", "description", "price"},
                new String[]{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
                new String[]{"1999", "Chevy", "Venture «Extended Edition»", "", "4900.00"},
                new String[]{"1996", "Jeep", "Grand Cherokee", "MUST SELL! air, moon roof, loaded", "4799.00"});
        List<Integer> expectedWidths = Arrays.asList(4, 5, 26, 33, 7);
        Table actual = Table.parseCSV(expectedCells, null);
        assertEquals(expectedCells, actual.cells);
        assertEquals(expectedWidths, actual.widths);
    }

    @Test
    public void testMakeDefaultHeaders() {
        List<String> expectedHeaders = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
                "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "aa", "bb", "cc", "dd", "ee", "ff", "gg");
        assertEquals(expectedHeaders.subList(0, 33), Utils.makeDefaultHeaders(33));
    }
}
