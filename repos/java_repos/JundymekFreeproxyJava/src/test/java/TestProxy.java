import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

class TestProxy {

    private Document mockDocument(String html) {
        return Jsoup.parse(html);
    }

    private List<Element> mockTrElements() {
        Document document = mockDocument(
                "<table id='proxylisttable'><tbody>" +
                        "<tr>" +
                        "<td>111.111.111.111</td><td>8080</td><td>CN</td><td class=\"hm\">China</td><td>anonymous</td>" +
                        "<td class=\"hm\">no</td><td class=\"hx\">yes</td><td class=\"hm\">1 min ago</td>" +
                        "</tr> <tr>" +
                        "<td>222.222.222.222</td><td>8080</td><td>NL</td><td class=\"hm\">Netherlands</td><td>elite proxy</td>" +
                        "<td class=\"hm\">yes</td><td class=\"hx\">no</td><td class=\"hm\">2 mins ago</td>" +
                        "</tr></tbody></table>"
        );
        return document.select("tr");
    }

    @Test
    void testEmptyProxyList() throws FreeProxyException {
        FreeProxy test = new FreeProxy();
        FreeProxy spyFreeProxy = spy(test);
        doReturn(Arrays.asList()).when(spyFreeProxy).getProxyList(anyBoolean());
        assertThrows(FreeProxyException.class, () -> spyFreeProxy.get(false), "There are no working proxies at this time.");
    }

    @Test
    void testInvalidProxy() throws FreeProxyException {
        FreeProxy test = new FreeProxy();
        FreeProxy spyFreeProxy = spy(test);
        doReturn(Arrays.asList("111.111.11:2222")).when(spyFreeProxy).getProxyList(anyBoolean());
        assertThrows(FreeProxyException.class, () -> spyFreeProxy.get(false), "There are no working proxies at this time.");
    }

    @Test
    void testAnonymFilter() throws FreeProxyException {
        FreeProxy test1 = new FreeProxy();
        int cnt1 = test1.getProxyList(false).size();
        FreeProxy test2 = new FreeProxy(null, 0.5, false, true, false, null, false);
        int cnt2 = test2.getProxyList(false).size();
        assertTrue(cnt2 < cnt1);
    }

    @Test
    void testEliteFilter() throws FreeProxyException {
        FreeProxy test1 = new FreeProxy();
        int cnt1 = test1.getProxyList(false).size();
        FreeProxy test2 = new FreeProxy(null, 0.5, false, false, true, null, false);
        int cnt2 = test2.getProxyList(false).size();
        assertTrue(cnt2 < cnt1);
    }

    @Test
    void testGoogleFilter() throws FreeProxyException {
        FreeProxy test1 = new FreeProxy();
        int cnt1 = test1.getProxyList(false).size();
        FreeProxy test2 = new FreeProxy(null, 0.5, false, false, false, true, false);
        FreeProxy test3 = new FreeProxy(null, 0.5, false, false, false, false, false);
        int cnt2 = test2.getProxyList(false).size();
        int cnt3 = test3.getProxyList(false).size();
        assertTrue(cnt2 < cnt1);
        assertTrue(cnt3 < cnt1);
    }

    @Test
    void testCriteriaDefaults() throws FreeProxyException {
        FreeProxy test = new FreeProxy();
        List<Element> elements = mockTrElements();
        assertTrue(test.criteria(elements.get(0)));
        assertTrue(test.criteria(elements.get(1)));
    }

    @Test
    void testCriteriaAnonymTrue() throws FreeProxyException {
        FreeProxy test = new FreeProxy(null, 0.5, false, true, false, null, false);
        List<Element> elements = mockTrElements();
        assertTrue(test.criteria(elements.get(0)));
        assertFalse(test.criteria(elements.get(1)));
    }

    @Test
    void testCriteriaEliteTrue() throws FreeProxyException {
        FreeProxy test = new FreeProxy(null, 0.5, false, false, true, null, false);
        List<Element> elements = mockTrElements();
        assertFalse(test.criteria(elements.get(0)));
        assertTrue(test.criteria(elements.get(1)));
    }

    @Test
    void testCriteriaGoogleFalse() throws FreeProxyException {
        FreeProxy test = new FreeProxy(null, 0.5, false, false, false, false, false);
        List<Element> elements = mockTrElements();
        assertTrue(test.criteria(elements.get(0)));
        assertFalse(test.criteria(elements.get(1)));
    }

    @Test
    void testCriteriaGoogleTrue() throws FreeProxyException {
        FreeProxy test = new FreeProxy(null, 0.5, false, false, false, true, false);
        List<Element> elements = mockTrElements();
        assertFalse(test.criteria(elements.get(0)));
        assertTrue(test.criteria(elements.get(1)));
    }

    @Test
    void testCriteriaHttpsTrue() throws FreeProxyException {
        FreeProxy test = new FreeProxy(null, 0.5, false, false, false, null, true);
        List<Element> elements = mockTrElements();
        assertTrue(test.criteria(elements.get(0)));
        assertFalse(test.criteria(elements.get(1)));
    }

    @Test
    void testCountryIdUsPageFirstLoop() throws FreeProxyException {
        FreeProxy test = new FreeProxy(Arrays.asList("US"), 0.5, false, false, false, null, false);
        assertEquals("https://www.us-proxy.org", test.getWebsite(false));
    }

    @Test
    void testCountryIdUsPageSecondLoop() throws FreeProxyException {
        FreeProxy test = new FreeProxy(Arrays.asList("US"), 0.5, false, false, false, null, false);
        assertEquals("https://free-proxy-list.net", test.getWebsite(true));
    }

    @Test
    void testCountryIdGbPageFirstLoop() throws FreeProxyException {
        FreeProxy test = new FreeProxy(Arrays.asList("GB"), 0.5, false, false, false, null, false);
        assertEquals("https://free-proxy-list.net/uk-proxy.html", test.getWebsite(false));
    }

    @Test
    void testCountryIdGbPageSecondLoop() throws FreeProxyException {
        FreeProxy test = new FreeProxy(Arrays.asList("GB"), 0.5, false, false, false, null, false);
        assertEquals("https://free-proxy-list.net", test.getWebsite(true));
    }

    @Test
    void testDefaultPageFirstLoop() throws FreeProxyException {
        FreeProxy test = new FreeProxy();
        assertEquals("https://www.sslproxies.org", test.getWebsite(false));
    }

    @Test
    void testDefaultPageSecondLoop() throws FreeProxyException {
        FreeProxy test = new FreeProxy();
        assertEquals("https://free-proxy-list.net", test.getWebsite(true));
    }
}

