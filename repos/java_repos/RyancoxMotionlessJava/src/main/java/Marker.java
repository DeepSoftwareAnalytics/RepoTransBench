import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Marker {
    private static final List<String> SIZES = Arrays.asList("tiny", "mid", "small");
    private static final String LABELS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private String size;
    private String color;
    private String label;
    private String iconUrl;

    public Marker(String size, String color, String label, String iconUrl) {
        if (size != null && !SIZES.contains(size)) {
            throw new IllegalArgumentException(
                String.format("[%s] is not a valid marker size. Valid sizes include %s", size, SIZES)
            );
        }
        if (label != null && (label.length() != 1 || !LABELS.contains(label))) {
            throw new IllegalArgumentException(
                String.format("[%s] is not a valid label. Valid labels are a single character 'A'..'Z' or '0'..'9'", label)
            );
        }
        if (color != null && !Color.isValidColor(color)) {
            throw new IllegalArgumentException(
                String.format("[%s] is not a valid color. Valid colors include %s", color, Color.COLORS)
            );
        }
        this.size = size;
        this.color = color;
        this.label = label;
        this.iconUrl = (iconUrl != null) ? URLEncoder.encode(iconUrl, StandardCharsets.UTF_8) : null;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }
}

class AddressMarker extends Marker {
    private String address;

    public AddressMarker(String address, String size, String color, String label, String iconUrl) {
        super(size, color, label, iconUrl);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}

class LatLonMarker extends Marker {
    private final String latitude;
    private final String longitude;

    public LatLonMarker(String lat, String lon, String size, String color, String label, String iconUrl) {
        super(size, color, label, iconUrl);
        this.latitude = lat;
        this.longitude = lon;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
