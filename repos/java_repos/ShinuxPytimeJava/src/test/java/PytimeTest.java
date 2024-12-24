import static org.junit.Assert.*;

import java.time.*;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestPyTime {

    private Pytime pytime;
    private int gmt8offset;
    private int currentYear;

    @Before
    public void setUp() {
        pytime = new Pytime();
        gmt8offset = ZoneOffset.ofHours(8).getTotalSeconds();
        currentYear = LocalDateTime.now().getYear();
    }

    @Test
    public void testParse() {
        assertTrue(pytime.parse("2015-5-17").equals(LocalDate.of(2015, 5, 17)));
        assertTrue(pytime.parse("2015/5/17").equals(LocalDate.of(2015, 5, 17)));
        assertTrue(pytime.parse("2015-5-17 23:23:23").equals(LocalDateTime.of(2015, 5, 17, 23, 23, 23)));
        assertTrue(pytime.parse("15-5-17 23:23:23").equals(LocalDateTime.of(2015, 5, 17, 23, 23, 23)));
        assertTrue(pytime.parse("2015517").equals(LocalDate.of(2015, 5, 17)));
        assertTrue(pytime.parse("2015.5.17").equals(LocalDate.of(2015, 5, 17)));
        assertTrue(pytime.parse("15-5-17").equals(LocalDate.of(2015, 5, 17)));
        assertTrue(pytime.parse("15.5.17").equals(LocalDate.of(2015, 5, 17)));
        assertTrue(pytime.parse("15/5/17").equals(LocalDate.of(2015, 5, 17)));
        assertTrue(pytime.parse("5/17/2015").equals(LocalDate.of(2015, 5, 17)));
        assertTrue(pytime.parse("17/5/2015").equals(LocalDate.of(2015, 5, 17)));
        assertTrue(pytime.parse("15517 23:23:23").equals(LocalDateTime.of(2015, 5, 17, 23, 23, 23)));
        assertTrue(pytime.parse("2015517 23:23:23").equals(LocalDateTime.of(2015, 5, 17, 23, 23, 23)));
        assertTrue(pytime.parse(1420041600L + gmt8offset).equals(LocalDateTime.of(2015, 1, 1, 0, 0)));
    }

    @Test
    public void testCount() {
        assertEquals(pytime.count("2015517", "2015519"), Duration.ofDays(-2));
        assertEquals(pytime.count("2015517", "2015519 23:23:23"), Duration.ofDays(-3).plusSeconds(2197));
        assertEquals(pytime.count("2015517 23:23:23", "2015519 23:23:23"), Duration.ofDays(-2));
        assertEquals(pytime.count("2015519 23:23:23", "2015-5-17"), Duration.ofDays(2).plusSeconds(84203));
    }

    @Test
    public void testFunction() {
        assertTrue(pytime.today().equals(LocalDate.now()));
        assertTrue(pytime.today(2014).equals(LocalDate.now().withYear(2014)));
        assertTrue(pytime.tomorrow().equals(LocalDate.now().plusDays(1)));
        assertTrue(pytime.tomorrow("2015-5-19").equals(LocalDate.of(2015, 5, 20)));
        assertTrue(pytime.yesterday().equals(LocalDate.now().minusDays(1)));
        assertTrue(pytime.yesterday("2015-5-29").equals(LocalDate.of(2015, 5, 28)));
        assertTrue(pytime.yesterday(1432310400L + gmt8offset).equals(LocalDateTime.of(2015, 5, 22, 0, 0)));
        assertTrue(pytime.tomorrow(1432310400L + gmt8offset).equals(LocalDateTime.of(2015, 5, 24, 0, 0)));
    }

    @Test
    public void testMethod() {
        assertArrayEquals(
            new LocalDate[]{
                LocalDate.of(2015, 5, 21),
                LocalDate.of(2015, 5, 20),
                LocalDate.of(2015, 5, 19),
                LocalDate.of(2015, 5, 18),
                LocalDate.of(2015, 5, 17)
            },
            pytime.daysRange("2015-5-17", "2015-5-21").toArray()
        );

        assertArrayEquals(
            new LocalDate[]{
                LocalDate.of(2015, 5, 20),
                LocalDate.of(2015, 5, 19),
                LocalDate.of(2015, 5, 18)
            },
            pytime.daysRange("2015-5-17", "2015-5-21", true).toArray()
        );

        assertEquals(pytime.lastDay(2015, 5), LocalDate.of(2015, 5, 31));
        assertEquals(pytime.lastDay(currentYear), pytime.lastDay());
        assertEquals(pytime.lastDay(currentYear, 6), pytime.lastDay(currentYear, 6));

        assertEquals(pytime.midnight("2015-5-17"), LocalDateTime.of(2015, 5, 17, 0, 0));
        assertEquals(pytime.midnight(), LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT));
        assertEquals(pytime.before("2015-5-17", "3days 3hou 2mi 1s"), LocalDateTime.of(2015, 5, 13, 20, 57, 59));
        assertEquals(pytime.before("2015-5-17 23:23:23", "2ye 3mon 2dy 1s"), LocalDateTime.of(2013, 2, 14, 23, 59, 59));

        assertEquals(pytime.after("2015-5-17", "32month 2days 1years"), LocalDateTime.of(2019, 1, 19, 0, 0));
        assertEquals(pytime.after("2015-5-17 23:23:23", "59days 280minu, 22222sec"), LocalDateTime.of(2015, 7, 15, 10, 50, 22));
        assertEquals(pytime.after("2015-5-17", "59days 9week"), LocalDateTime.of(2015, 9, 16, 0, 0));
        assertEquals(pytime.before("2015-5-17", "5y 6m 7w 8d 9h 10mi 59s"), LocalDateTime.of(2009, 9, 20, 14, 49, 1));

        assertEquals(pytime.thisWeek("2015-5-17"), new LocalDate[]{LocalDate.of(2015, 5, 11), LocalDate.of(2015, 5, 18)});
        assertEquals(pytime.thisWeek("2015-5-17", true), new LocalDate[]{LocalDate.of(2015, 5, 11), LocalDate.of(2015, 5, 17)});
        assertEquals(pytime.lastWeek("2015-5-17"), new LocalDate[]{LocalDate.of(2015, 5, 4), LocalDate.of(2015, 5, 12)});
        assertEquals(pytime.lastWeek("2015-5-17", true), new LocalDate[]{LocalDate.of(2015, 5, 4), LocalDate.of(2015, 5, 11)});
        assertEquals(pytime.nextWeek("2015-5-17"), new LocalDate[]{LocalDate.of(2015, 5, 18), LocalDate.of(2015, 5, 26)});
        assertEquals(pytime.nextWeek("2015-5-17", true), new LocalDate[]{LocalDate.of(2015, 5, 18), LocalDate.of(2015, 5, 25)});
        assertEquals(pytime.thisMonth("2015-5-17"), new LocalDate[]{LocalDate.of(2015, 5, 1), LocalDate.of(2015, 6, 1)});
        assertEquals(pytime.thisMonth("2015-5-17", true), new LocalDate[]{LocalDate.of(2015, 5, 1), LocalDate.of(2015, 5, 31)});
        assertEquals(pytime.lastMonth("2015-5-17"), new LocalDate[]{LocalDate.of(2015, 4, 1), LocalDate.of(2015, 5, 1)});
        assertEquals(pytime.lastMonth("2015-5-17", true), new LocalDate[]{LocalDate.of(2015, 4, 1), LocalDate.of(2015, 4, 30)});
        assertEquals(pytime.nextMonth("2015-5-17"), new LocalDate[]{LocalDate.of(2015, 6, 1), LocalDate.of(2015, 7, 1)});
        assertEquals(pytime.nextMonth("2015-5-17", true), new LocalDate[]{LocalDate.of(2015, 6, 1), LocalDate.of(2015, 6, 30)});
    }

    @Test
    public void testFestival() {
        assertEquals(pytime.newYear(2015), LocalDate.of(2015, 1, 1));
        assertEquals(pytime.valentine(2014), LocalDate.of(2014, 2, 14));
        assertEquals(pytime.fool(2013), LocalDate.of(2013, 4, 1));
        assertEquals(pytime.christmas(2012), LocalDate.of(2012, 12, 25));
        assertEquals(pytime.christmasEve(2011), LocalDate.of(2011, 12, 24));
        assertEquals(pytime.mother(2010), LocalDate.of(2010, 5, 9));
        assertEquals(pytime.father(2009), LocalDate.of(2009, 6, 21));
        assertEquals(pytime.halloween(2008), LocalDate.of(2008, 10, 31));
        assertEquals(pytime.easter(2007), LocalDate.of(2007, 4, 8));
        assertEquals(pytime.thanksgiving(2006), LocalDate.of(2006, 11, 23));

        assertEquals(pytime.vatertag(2020), LocalDate.of(2020, 5, 21));
        assertEquals(pytime.vatertag(2021), LocalDate.of(2021, 5, 13));
        assertEquals(pytime.vatertag(2025), LocalDate.of(2025, 5, 29));
    }

    @Test
    public void testFromStr() {
        try {
            pytime.parse("App.19st,2015");
            fail("Expected CanNotFormatError");
        } catch (CanNotFormatError e) {
            // Test passes if this exception is caught
        }

        assertEquals(pytime.parse("Jan.1 st, 2015"), LocalDate.of(2015, 1, 1));
        assertEquals(pytime.parse("January 2nd 2015"), LocalDate.of(2015, 1, 2));
        assertEquals(pytime.parse("Jan, 3rd 2015"), LocalDate.of(2015, 1, 3));
        assertEquals(pytime.parse("Jan.2st,2015"), LocalDate.of(2015, 1, 2));
        assertEquals(pytime.parse("Feb.19st,2015"), LocalDate.of(2015, 2, 19));
        assertEquals(pytime.parse("Mar.19st,2015"), LocalDate.of(2015, 3, 19));
        assertEquals(pytime.parse("Apr.19st,2015"), LocalDate.of(2015, 4, 19));
        assertEquals(pytime.parse("May.19st,2015"), LocalDate.of(2015, 5, 19));
        assertEquals(pytime.parse("Jun.19st,2015"), LocalDate.of(2015, 6, 19));
        assertEquals(pytime.parse("Jul.19st,2014"), LocalDate.of(2014, 7, 19));
        assertEquals(pytime.parse("Aug.19st,2015"), LocalDate.of(2015, 8, 19));
        assertEquals(pytime.parse("Sep.19st,2015"), LocalDate.of(2015, 9, 19));
        assertEquals(pytime.parse("Oct.19st,2015"), LocalDate.of(2015, 10, 19));
        assertEquals(pytime.parse("Nov.19st,2015"), LocalDate.of(2015, 11, 19));
        assertEquals(pytime.parse("Dec.19st,2014"), LocalDate.of(2014, 12, 19));
    }
}
