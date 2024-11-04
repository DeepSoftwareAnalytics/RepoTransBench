import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Country {
    private List<String> topLevelDomain;
    private String alpha2Code;
    private String alpha3Code;
    private List<Map<String, String>> currencies;
    private String capital;
    private List<String> callingCodes;
    private List<String> altSpellings;
    private String relevance;
    private String region;
    private String subregion;
    private Map<String, String> translations;
    private int population;
    private List<Double> latlng;
    private String demonym;
    private double area;
    private double gini;
    private List<String> timezones;
    private List<String> borders;
    private String nativeName;
    private String name;
    private String numericCode;
    private List<Map<String, String>> languages;
    private String flag;
    private List<Map<String, Object>> regionalBlocs;
    private String cioc;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(numericCode, country.numericCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numericCode);
    }

    @Override
    public String toString() {
        return "<" + name + " | " + alpha3Code + ">";
    }
}

