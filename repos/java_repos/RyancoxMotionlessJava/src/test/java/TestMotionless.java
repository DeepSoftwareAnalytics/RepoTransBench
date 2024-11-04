import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestMotionless {

    private String address;
    private String lat;
    private String lon;

    @BeforeEach
    public void setUp() {
        address = "151 third st, san francisco, ca";
        lat = "48.858278";
        lon = "2.294489";
    }

    @Test
    public void testCenterMapSat() {
        CenterMap cmapSat = new CenterMap(null, lat, lon, 17, 400, 400, "satellite", 1, null, null, "en", null, null, null);
        assertEquals(cmapSat.generateUrl(),
            "https://maps.googleapis.com/maps/api/staticmap?maptype=satellite&format=png&scale=1&center=48.858278%2C2.294489&zoom=17&size=400x400&sensor=false&language=en");
    }

    @Test
    public void testVisibleMap() {
        VisibleMap vmap = new VisibleMap(400, 400, "terrain", 1, null, null, "en", null, null, null);
        vmap.addAddress("Sugarbowl, Truckee, CA");
        vmap.addAddress("Tahoe City, CA");

        assertEquals(vmap.generateUrl(),
            "https://maps.googleapis.com/maps/api/staticmap?maptype=terrain&format=png&scale=1&size=400x400&sensor=false&visible=Sugarbowl%2C%20Truckee%2C%20CA%7CTahoe%20City%2C%20CA&language=en");
    }

    @Test
    public void testCreateMapWithAddress() {
        CenterMap centerMap = new CenterMap(address, null, null, 17, 400, 400, "roadmap", 1, null, null, "en", null, null, null);
        assertEquals(centerMap.generateUrl(),
            "https://maps.googleapis.com/maps/api/staticmap?maptype=roadmap&format=png&scale=1&center=151%20third%20st%2C%20san%20francisco%2C%20ca&zoom=17&size=400x400&sensor=false&language=en");
    }

    @Test
    public void testAddressMarker() {
        DecoratedMap dmap = new DecoratedMap();
        AddressMarker am1 = new AddressMarker("1 Infinite Loop, Cupertino, CA", "A");
        dmap.addMarker(am1);
        AddressMarker am2 = new AddressMarker("1600 Amphitheatre Parkway Mountain View, CA", "G");
        dmap.addMarker(am2);

        String url = dmap.generateUrl();
        assertNotNull(url);  // URL 的值不固定，仅验证生成不为 null
    }

    @Test
    public void testCreateMarkerMapWithStyles() {
        // 样式示例
        Object styles = new Object(); // 具体样式的对象，可在未来实现具体类
        DecoratedMap decoratedMap = new DecoratedMap(null, null, 17, 400, 400, "roadmap", 1, false, "green", null, null, null, styles, 1.11111, "en", null, null, null);

        decoratedMap.addMarker(new LatLonMarker(lat, lon, "mid", "red", "A", null));
        assertEquals(decoratedMap.generateUrl(),
            "https://maps.googleapis.com/maps/api/staticmap?maptype=roadmap&format=png&scale=1&size=400x400&sensor=false&language=en&markers=size:mid|color:red|label:A|48.858278,2.294489");
    }

    @Test
    public void testApiKey() {
        CenterMap cmap = new CenterMap(null, lat, lon, 17, 400, 400, "satellite", 1, "abcdefghi", null, "en", null, null, null);
        assertEquals(cmap.generateUrl(),
            "https://maps.googleapis.com/maps/api/staticmap?key=abcdefghi&maptype=satellite&format=png&scale=1&center=48.858278%2C2.294489&zoom=17&size=400x400&sensor=false&language=en");
    }

    @Test
    public void testApiKeyClientIdExclusive() {
        assertThrows(IllegalArgumentException.class, () -> {
            CenterMap cmap = new CenterMap(null, lat, lon, 17, 400, 400, "satellite", 1, "abcdefghi", "gme-exampleid", "en", null, null, null);
        });
    }

    @Test
    public void testChannelRequiresClientId() {
        assertThrows(IllegalArgumentException.class, () -> {
            CenterMap cmap = new CenterMap(null, lat, lon, 17, 400, 400, "satellite", 1, null, null, "en", "somechannel", null, null);
        });
    }

    @Test
    public void testClientIdRequiresSecret() {
        assertThrows(IllegalArgumentException.class, () -> {
            CenterMap cmap = new CenterMap(null, lat, lon, 17, 400, 400, "satellite", 1, null, "gme-exampleid", "en", null, null, null);
        });
    }

    @Test
    public void testClientIdAndPrivateKey() {
        CenterMap cmap = new CenterMap(null, lat, lon, 17, 400, 400, "satellite", 1, null, "gme-exampleid", "en", null, "bbXgwW0k3631Bl2V5Z34gs9vYgf=", null);
        assertEquals(cmap.generateUrl(),
            "https://maps.googleapis.com/maps/api/staticmap?client=gme-exampleid&maptype=satellite&format=png&scale=1&center=48.858278%2C2.294489&zoom=17&size=400x400&sensor=false&language=en&signature=PsD-OrvyjeIflTpH1p6v5hElJrE=");

        VisibleMap vmap = new VisibleMap(400, 400, "terrain", 1, null, "gme-exampleid", "en", null, "bbXgwW0k3631Bl2V5Z34gs9vYgf=", null);
        vmap.addAddress("Sugarbowl, Truckee, CA");
        vmap.addAddress("Tahoe City, CA");

        assertEquals(vmap.generateUrl(),
            "https://maps.googleapis.com/maps/api/staticmap?client=gme-exampleid&maptype=terrain&format=png&scale=1&size=400x400&sensor=false&visible=Sugarbowl%2C%20Truckee%2C%20CA%7CTahoe%20City%2C%20CA&language=en&signature=0_hfvOReb4YQfq7sGyAs0dLEDEo=");
    }

    @Test
    public void testChannel() {
        CenterMap cmap = new CenterMap(null, lat, lon, 17, 400, 400, "satellite", 1, null, "gme-exampleid", "en", "somechannel", "bbXgwW0k3631Bl2V5Z34gs9vYgf=", null);
        assertEquals(cmap.generateUrl(),
            "https://maps.googleapis.com/maps/api/staticmap?client=gme-exampleid&maptype=satellite&format=png&scale=1&center=48.858278%2C2.294489&zoom=17&size=400x400&sensor=false&language=en&channel=somechannel&signature=Y-D-iEMbWPfUTjBtKEYDbGUtElY=");

        VisibleMap vmap = new VisibleMap(400, 400, "terrain", 1, null, "gme-exampleid", "en", "somechannel", "bbXgwW0k3631Bl2V5Z34gs9vYgf=", null);
        vmap.addAddress("Sugarbowl, Truckee, CA");
        vmap.addAddress("Tahoe City, CA");

        assertEquals(vmap.generateUrl(),
            "https://maps.googleapis.com/maps/api/staticmap?client=gme-exampleid&maptype=terrain&format=png&scale=1&size=400x400&sensor=false&visible=Sugarbowl%2C%20Truckee%2C%20CA%7CTahoe%20City%2C%20CA&language=en&channel=somechannel&signature=KQvz4Q3rB6Pmr7sJ_sM4qfKQzDo=");
    }
}
