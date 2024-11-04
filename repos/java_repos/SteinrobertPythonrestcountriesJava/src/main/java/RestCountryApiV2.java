import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RestCountryApiV2 {
    public static String BASE_URI = "https://restcountries.com/v2";
    private static final String QUERY_SEPARATOR = ";";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    private static List<Country> getCountryList(String resource, String term, List<String> filters) throws IOException {
        String filtersUriString = "";
        if (filters != null && !filters.isEmpty()) {
            String filterString = String.join(QUERY_SEPARATOR, filters);
            filtersUriString = "fields=" + filterString;
        }

        if (term != null && !term.isEmpty() && !resource.endsWith("=")) {
            term = "/" + term;
        }

        String uri = BASE_URI + resource + (term != null ? term : "");
        if (!filtersUriString.isEmpty()) {
            uri += (uri.contains("?") ? "&" : "?") + filtersUriString;
        }

        Request request = new Request.Builder().url(uri).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Type listType = new TypeToken<ArrayList<Country>>() {}.getType();

                // Check if the response is an array or a single object
                if (responseBody.trim().startsWith("[")) { 
                    return gson.fromJson(responseBody, listType);
                } else {
                    Country country = gson.fromJson(responseBody, Country.class);
                    return List.of(country);
                }

            } else if (response.code() == 404) {
                throw new IllegalArgumentException("Invalid URL");
            } else {
                throw new IOException("Request failed with code: " + response.code());
            }
        }
    }

    public static List<Country> getAll(List<String> filters) throws IOException {
        return getCountryList("/all", "", filters);
    }

    public static List<Country> getCountriesByName(String name, List<String> filters) throws IOException {
        return getCountryList("/name", name, filters);
    }

    public static List<Country> getCountriesByLanguage(String language, List<String> filters) throws IOException {
        return getCountryList("/lang", language, filters);
    }

    public static List<Country> getCountriesByCallingCode(String callingCode, List<String> filters) throws IOException {
        return getCountryList("/callingcode", callingCode, filters);
    }

    public static Country getCountryByCountryCode(String alpha, List<String> filters) throws IOException {
        List<Country> countries = getCountryList("/alpha", alpha, filters);
        return countries.isEmpty() ? null : countries.get(0);
    }

    public static List<Country> getCountriesByCountryCodes(List<String> codes, List<String> filters) throws IOException {
        String codesString = String.join(QUERY_SEPARATOR, codes);
        return getCountryList("/alpha?codes=", codesString, filters);
    }

    public static List<Country> getCountriesByCurrency(String currency, List<String> filters) throws IOException {
        return getCountryList("/currency", currency, filters);
    }

    public static List<Country> getCountriesByRegion(String region, List<String> filters) throws IOException {
        return getCountryList("/region", region, filters);
    }

    public static List<Country> getCountriesBySubregion(String subregion, List<String> filters) throws IOException {
        return getCountryList("/subregion", subregion, filters);
    }

    public static List<Country> getCountriesByCapital(String capital, List<String> filters) throws IOException {
        return getCountryList("/capital", capital, filters);
    }
}
