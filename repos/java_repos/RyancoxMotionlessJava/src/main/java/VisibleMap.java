import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VisibleMap extends Map {
    private final List<String> locations;

    public VisibleMap(int sizeX, int sizeY, String mapType, int scale, String key, Object style, String language, String clientId, String secret, String channel) {
        super(sizeX, sizeY, mapType, null, scale, key, language, style, clientId, secret, channel);
        this.locations = new ArrayList<>();
    }

    public void addAddress(String address) {
        locations.add(URLEncoder.encode(address, StandardCharsets.UTF_8));
    }

    public void addLatLon(String lat, String lon) {
        locations.add(lat + "," + lon);
    }

    @Override
    protected String generateUrl() {
        String visiblePart = String.join("|", locations);
        String query = String.format("mapType=%s&format=%s&scale=%d&size=%dx%d&sensor=%s&visible=%s&language=%s",
                mapType, "png", scale, sizeX, sizeY, "false", visiblePart, language);
        
        String urlBase = "https://maps.googleapis.com/maps/api/staticmap";
        return urlBase + "?" + query; // Constructing the full URL based on the requirements
    }
}
