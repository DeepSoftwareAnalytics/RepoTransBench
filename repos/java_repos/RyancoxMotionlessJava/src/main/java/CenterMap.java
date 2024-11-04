import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CenterMap extends Map {
    private String address;  // Define the address field
    private String lat;  // Define the lat field
    private String lon;  // Define the lon field

    public CenterMap(String address, String lat, String lon, int zoom, int sizeX, int sizeY, String mapType, int scale, String key, Object style, String language, String clientId, String secret, String channel) {
        super(sizeX, sizeY, mapType, zoom, scale, key, language, style, clientId, secret, channel);
        this.address = address;  // Initialize the address field
        this.lat = lat;  // Initialize the lat field
        this.lon = lon;  // Initialize the lon field
    }

    @Override
    protected String generateUrl() {
        String centerPart = (address != null) ? URLEncoder.encode(address, StandardCharsets.UTF_8) :
                (lat != null && lon != null) ? lat + "," + lon : "1600 Amphitheatre Parkway Mountain View, CA";
        String query = String.format("maptype=%s&format=%s&scale=%d&center=%s&zoom=%d&size=%dx%d&sensor=%s&language=%s",
                mapType, "png", scale, centerPart, zoom, sizeX, sizeY, "false", language);
        String urlBase = "https://maps.googleapis.com/maps/api/staticmap";
        return urlBase + "?" + query;  // Constructing the full URL based on the requirements
    }
}
