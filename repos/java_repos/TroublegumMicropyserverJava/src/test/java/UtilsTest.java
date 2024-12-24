import org.junit.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testGetRequestMethodShouldReturnGET() {
        String request = "GET / HTTP/1.1\r\nHost: localhost";
        assertEquals("GET", Utils.getRequestMethod(request));
    }

    @Test
    public void testGetRequestMethodShouldReturnPOST() {
        String request = "POST /post HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 0\r\n\r\n";
        assertEquals("POST", Utils.getRequestMethod(request));
    }

    @Test
    public void testGetRequestQueryStringShouldReturnString() {
        String request = "GET /?param_one=one&param_two=two HTTP/1.1\r\nHost: localhost\r\n\r\n";
        assertEquals("param_one=one&param_two=two", Utils.getRequestQueryString(request));
    }

    @Test
    public void testParseQueryStringShouldReturnTwoParams() {
        String queryString = "param_one=one&param_two=two";
        Map<String, String> expected = new HashMap<>();
        expected.put("param_one", "one");
        expected.put("param_two", "two");
        assertEquals(expected, Utils.parseQueryString(queryString));
    }

    @Test
    public void testGetRequestQueryParamsShouldReturnTwoParams() {
        String request = "GET /?param_one=one&param_two=two HTTP/1.1\r\nHost: localhost\r\n\r\n";
        Map<String, String> expected = new HashMap<>();
        expected.put("param_one", "one");
        expected.put("param_two", "two");
        assertEquals(expected, Utils.getRequestQueryParams(request));
    }

    @Test
    public void testGetRequestPostParamsShouldReturnTwoParams() {
        String request = "POST /post HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 27\r\n\r\nparam_one=one&param_two=two";
        Map<String, String> expected = new HashMap<>();
        expected.put("param_one", "one");
        expected.put("param_two", "two");
        assertEquals(expected, Utils.getRequestPostParams(request));
    }

    @Test
    public void testUnquoteShouldReturnEmptyString() {
        assertEquals("", Utils.unquote(""));
    }

    @Test
    public void testUnquoteShouldReturnString() {
        assertEquals("param", Utils.unquote("param"));
    }

    @Test
    public void testUnquoteShouldReturnUnquotedString() {
        assertEquals("параметр и значение %",
                Utils.unquote("%D0%BF%D0%B0%D1%80%D0%B0%D0%BC%D0%B5%D1%82%D1%80%20%D0%B8%20%D0%B7%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%20%25"));
    }
}
