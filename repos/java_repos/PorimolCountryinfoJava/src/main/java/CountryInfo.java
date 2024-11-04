import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class CountryInfo {
    private String countryName;
    private Map<String, JSONObject> countries;

    public CountryInfo(String countryName) {
        this.countryName = countryName.toLowerCase();
        loadCountries();
    }

    public CountryInfo() {
        this.countryName = "";
        loadCountries();
    }

    private void loadCountries() {
        countries = new HashMap<>();
        String dirPath = Paths.get("src/main/java/data/").toAbsolutePath().toString();
        try {
            Files.list(Paths.get(dirPath)).forEach(path -> {
                if (Files.isRegularFile(path) && path.toString().endsWith(".json")) {
                    try {
                        String content = new String(Files.readAllBytes(path));
                        JSONObject countryInfo = new JSONObject(content);
                        String name = countryInfo.getString("name").toLowerCase();
                        countries.put(name, countryInfo);
                        JSONArray altSpellings = countryInfo.getJSONArray("altSpellings");
                        for (int i = 0; i < altSpellings.length(); i++) {
                            if (altSpellings.getString(i).equalsIgnoreCase(this.countryName)) {
                                this.countryName = name;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<JSONObject> info() {
        return Optional.ofNullable(countries.get(countryName));
    }

    public Optional<JSONArray> provinces() {
        return getJsonArrayValue("provinces");
    }

    public Optional<JSONObject> iso() {
        return getJsonObjectValue("ISO");
    }

    public Optional<String> iso(int alpha) {
        return iso().map(isoObject -> isoObject.optString("alpha" + alpha));
    }

    public Optional<JSONArray> altSpellings() {
        return getJsonArrayValue("altSpellings");
    }

    public Optional<Integer> area() {
        return getJsonIntValue("area");
    }

    public Optional<JSONArray> borders() {
        return getJsonArrayValue("borders");
    }

    public Optional<JSONArray> callingCodes() {
        return getJsonArrayValue("callingCodes");
    }

    public Optional<String> capital() {
        return getJsonStringValue("capital");
    }

    public Optional<JSONArray> capitalLatlng() {
        return getJsonArrayValue("capital_latlng");
    }

    public Optional<JSONArray> currencies() {
        return getJsonArrayValue("currencies");
    }

    public Optional<String> demonym() {
        return getJsonStringValue("demonym");
    }

    public Optional<String> flag() {
        return getJsonStringValue("flag");
    }

    public Optional<JSONObject> geoJson() {
        return getJsonObjectValue("geoJSON");
    }

    public Optional<JSONArray> languages() {
        return getJsonArrayValue("languages");
    }

    public Optional<JSONArray> latlng() {
        return getJsonArrayValue("latlng");
    }

    public Optional<String> name() {
        return Optional.ofNullable(countryName);
    }

    public Optional<String> nativeName() {
        return getJsonStringValue("nativeName");
    }

    public Optional<Integer> population() {
        return getJsonIntValue("population");
    }

    public Optional<String> region() {
        return getJsonStringValue("region");
    }

    public Optional<String> subregion() {
        return getJsonStringValue("subregion");
    }

    public Optional<JSONArray> timezones() {
        return getJsonArrayValue("timezones");
    }

    public Optional<JSONArray> tld() {
        return getJsonArrayValue("tld");
    }

    public Optional<JSONObject> translations() {
        return getJsonObjectValue("translations");
    }

    public Optional<String> wiki() {
        return getJsonStringValue("wiki");
    }

    public Optional<String> google() {
        return name().map(n -> "https://www.google.com/search?q=" + n);
    }

    public Map<String, JSONObject> all() {
        return countries;
    }

    private Optional<String> getJsonStringValue(String key) {
        return Optional.ofNullable(countries.get(countryName)).map(c -> c.optString(key, null));
    }

    private Optional<Integer> getJsonIntValue(String key) {
        return Optional.ofNullable(countries.get(countryName)).map(c -> c.optInt(key, 0));
    }

    private Optional<JSONArray> getJsonArrayValue(String key) {
        return Optional.ofNullable(countries.get(countryName)).map(c -> c.optJSONArray(key));
    }

    private Optional<JSONObject> getJsonObjectValue(String key) {
        return Optional.ofNullable(countries.get(countryName)).map(c -> c.optJSONObject(key));
    }

    public static void main(String[] args) {
        CountryInfo country = new CountryInfo("Singapore");
        country.all().forEach((name, info) -> System.out.println("Country: " + name));
    }
}
