import java.util.*;
import java.util.stream.Collectors;

public class DecoratedMap extends Map {
    private final List<Marker> markers;
    private final List<String> path;
    private final String fillColor;
    private final String pathWeight;
    private final String pathColor;
    private final boolean region;

    public DecoratedMap(String lat, String lon, Integer zoom, int sizeX, int sizeY, String mapType, int scale, boolean region, String fillColor, String pathWeight, String pathColor, String key, Object style, double simplifyThresholdMeters, String language, String clientId, String secret, String channel) {
        super(sizeX, sizeY, mapType, zoom, scale, key, language, style, clientId, secret, channel);
        this.markers = new ArrayList<>();
        this.path = new ArrayList<>();
        this.fillColor = fillColor;
        this.pathWeight = pathWeight;
        this.pathColor = pathColor;
        this.region = region;
    }

    public void addMarker(Marker marker) {
        markers.add(marker);
    }

    public void addPathLatLon(String lat, String lon) {
        path.add(lat + "," + lon);
    }

    @Override
    protected String generateUrl() {
        String query = String.format("mapType=%s&format=%s&scale=%d&size=%dx%d&sensor=%s&language=%s",
                mapType, "png", scale, sizeX, sizeY, "false", language);

        // Append markers and path to query string
        if (!markers.isEmpty()) {
            query += "&" + markers.stream()
                    .map(marker -> {
                        if (marker instanceof LatLonMarker) {
                            LatLonMarker llm = (LatLonMarker) marker;
                            return String.format("markers=size:%s|color:%s|label:%s|%s",
                                    marker.getSize(), marker.getColor(), marker.getLabel(), llm.getLatitude() + "," + llm.getLongitude());
                        } else {
                            return String.format("markers=size:%s|color:%s|label:%s|%s",
                                    marker.getSize(), marker.getColor(), marker.getLabel(), ((AddressMarker) marker).getAddress());
                        }
                    })
                    .collect(Collectors.joining("&"));
        }

        if (!path.isEmpty()) {
            query += "&path=color:" + pathColor + "|weight:" + pathWeight + "|" + String.join("|", path);
        }

        return query;
    }
}
