import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class Tests {
    static Map<String, CountryInfo> allCountries = new HashMap<>();

    @BeforeAll
    public static void setUp() {
        System.out.println("Loading all countries...");
        CountryInfo ci = new CountryInfo();
        ci.all().forEach((name, info) -> allCountries.put(name, new CountryInfo(name)));
    }

    @Test
    public void testAllCountriesHaveName() {
        allCountries.forEach((name, country) -> {
            try {
                assertNotNull(country.name().get(), "Country name should not be null");
            } catch (Exception e) {
                fail("Country '" + name + "' threw an exception: " + e.getMessage());
            }
        });
    }

    @Test
    public void testAllCountriesHaveNativeName() {
        allCountries.forEach((name, country) -> {
            try {
                assertNotNull(country.nativeName().get(), "Native name should not be null");
            } catch (Exception e) {
                fail("Country '" + name + "' threw an exception: " + e.getMessage());
            }
        });
    }

    @Test
    public void testAllCountriesHaveISO() {
        allCountries.forEach((name, country) -> {
            try {
                assertNotNull(country.iso(), "ISO should not be null");
            } catch (Exception e) {
                fail("Country '" + name + "' threw an exception: " + e.getMessage());
            }
        });
    }

    @Test
    public void testAllCountriesHaveAltSpellings() {
        allCountries.forEach((name, country) -> {
            try {
                assertNotNull(country.altSpellings().get(), "Alt spellings should not be null");
            } catch (Exception e) {
                fail("Country '" + name + "' threw an exception: " + e.getMessage());
            }
        });
    }

    @Test
    public void testAllCountriesHaveTranslations() {
        allCountries.forEach((name, country) -> {
            try {
                assertNotNull(country.translations().get(), "Translations should not be null");
            } catch (Exception e) {
                fail("Country '" + name + "' threw an exception: " + e.getMessage());
            }
        });
    }

    @Test
    public void testSelectCountryFromAltName() {
        CountryInfo country = new CountryInfo("PK");
        assertNotNull(country.name().orElse(null), "Country name should be 'pakistan'");
    }
}
