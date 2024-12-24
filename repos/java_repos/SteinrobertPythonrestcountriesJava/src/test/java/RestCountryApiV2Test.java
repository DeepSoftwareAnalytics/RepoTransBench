import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RestCountryApiV2Test {
    private MockWebServer mockWebServer;
    private static final String BASE_URI = "https://restcountries.com/v2";

    @BeforeEach
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        RestCountryApiV2.BASE_URI = mockWebServer.url("/").toString();
    }

    @Test
    public void testGetAll() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"},{\"name\":\"Nigeria\"},{\"name\":\"Egypt\"},{\"name\":\"Kenya\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getAll(null);
        assertEquals(4, countries.size());
    }

    @Test
    public void testGetAllWithFilter() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\",\"capital\":\"Pretoria\"},{\"name\":\"Nigeria\",\"capital\":\"Abuja\"},{\"name\":\"Egypt\",\"capital\":\"Cairo\"},{\"name\":\"Kenya\",\"capital\":\"Nairobi\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getAll(List.of("name", "capital"));
        assertEquals(4, countries.size());
    }

    @Test
    public void testGetCountriesByName() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByName("South Africa", null);
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountriesByNameWithFilter() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\",\"currencies\":[{\"code\":\"ZAR\"}]}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByName("South Africa", List.of("name", "currencies"));
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountryByCountryCode() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("{\"name\":\"South Africa\"}").setResponseCode(200));
        Country country = RestCountryApiV2.getCountryByCountryCode("za", null);
        assertEquals("South Africa", country.getName());
    }

    @Test
    public void testGetCountryByCountryCodeWithFilter() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("{\"name\":\"South Africa\",\"capital\":\"Pretoria\"}").setResponseCode(200));
        Country country = RestCountryApiV2.getCountryByCountryCode("za", List.of("name", "capital"));
        assertEquals("South Africa", country.getName());
    }

    @Test
    public void testGetCountriesByCountryCodes() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"Nigeria\"},{\"name\":\"Egypt\"},{\"name\":\"Kenya\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByCountryCodes(List.of("ng", "eg", "ken"), null);
        assertEquals(3, countries.size());
    }

    @Test
    public void testGetCountriesByCountryCodesWithFilter() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"Nigeria\",\"currencies\":[{\"code\":\"NGN\"}]},{\"name\":\"Egypt\",\"currencies\":[{\"code\":\"EGP\"}]},{\"name\":\"Kenya\",\"currencies\":[{\"code\":\"KES\"}]}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByCountryCodes(List.of("ng", "eg", "ken"), List.of("name", "currencies"));
        assertEquals(3, countries.size());
    }

    @Test
    public void testGetCountriesByCurrency() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByCurrency("ZAR", null);
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountriesByCurrencyWithFilter() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByCurrency("ZAR", List.of("name"));
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountriesByLanguage() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"},{\"name\":\"Nigeria\"},{\"name\":\"Kenya\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByLanguage("en", null);
        assertEquals(3, countries.size());
    }

    @Test
    public void testGetCountriesByLanguageWithFilter() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\",\"flag\":\"https://restcountries.eu/data/zaf.svg\"},{\"name\":\"Nigeria\",\"flag\":\"https://restcountries.eu/data/nga.svg\"},{\"name\":\"Kenya\",\"flag\":\"https://restcountries.eu/data/ken.svg\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByLanguage("en", List.of("name", "flag"));
        assertEquals(3, countries.size());
    }

    @Test
    public void testGetCountriesByCapital() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByCapital("Pretoria", null);
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountriesByCapitalWithFilter() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\",\"demonym\":\"South African\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByCapital("Pretoria", List.of("name", "demonym"));
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountriesByCallingCode() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByCallingCode("27", null);
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountriesByCallingCodeWithFilter() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByCallingCode("27", List.of("name"));
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountriesByRegion() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"},{\"name\":\"Nigeria\"},{\"name\":\"Egypt\"},{\"name\":\"Kenya\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByRegion("africa", null);
        assertEquals(4, countries.size());
    }

    @Test
    public void testGetCountriesByRegionWithFilter() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"},{\"name\":\"Nigeria\"},{\"name\":\"Egypt\"},{\"name\":\"Kenya\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesByRegion("africa", List.of("name"));
        assertEquals(4, countries.size());
    }

    @Test
    public void testGetCountriesBySubregion() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesBySubregion("southern africa", null);
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountriesBySubregionWithFilter() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("[{\"name\":\"South Africa\"}]").setResponseCode(200));
        List<Country> countries = RestCountryApiV2.getCountriesBySubregion("southern africa", List.of("name"));
        assertEquals(1, countries.size());
    }
}
