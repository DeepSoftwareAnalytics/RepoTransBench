import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class MicrodataParserTest {

    @Test
    public void testParse() throws IOException, URISyntaxException {
        // 通过类路径加载文件
        ClassLoader classLoader = getClass().getClassLoader();
        URI uri = classLoader.getResource("test-data/example.html").toURI();
        List<Item> items = Microdata.getItems(uri.getPath());

        assertEquals(1, items.size());

        Item item = items.get(0);

        assertEquals(List.of(new URI("http://schema.org/Person")), item.getItemtype());

        assertEquals("Jane Doe", item.get("name"));

        assertEquals(new URI("http://www.xyz.edu/students/alicejones.html"), item.get("colleagues"));

        assertEquals(List.of(
            new URI("http://www.xyz.edu/students/alicejones.html"),
            new URI("http://www.xyz.edu/students/bobsmith.html")),
            item.getAll("colleagues"));

        Item address = (Item) item.get("address");
        assertNotNull(address);
        assertEquals(List.of(new URI("http://schema.org/PostalAddress")), address.getItemtype());
        assertEquals("Seattle", address.get("addressLocality"));

        assertFalse(address.get("streetAddress").toString().contains("Unrelated text"));

        String json = item.toJson();
        Item gsonItem = Item.fromJson(json);
        assertEquals("Jane Doe", ((List<?>)gsonItem.getProperties().get("name")).get(0));
        assertEquals(List.of("http://schema.org/Person"), gsonItem.getItemtype());
        assertEquals("http://www.xyz.edu/~jane", gsonItem.getItemid().getUri());
        assertTrue(gsonItem.getProperties().get("address").get(0) instanceof Item);
        assertEquals("Seattle", ((Item)gsonItem.getProperties().get("address").get(0))
            .getProperties().get("addressLocality").get(0));
    }

    @Test
    public void testParseNested() throws IOException, URISyntaxException {
        // 通过类路径加载文件
        ClassLoader classLoader = getClass().getClassLoader();
        URI uri = classLoader.getResource("test-data/example-nested.html").toURI();
        List<Item> items = Microdata.getItems(uri.getPath());

        assertEquals(1, items.size());

        Item item = items.get(0);

        assertEquals(List.of(new URI("http://schema.org/Event")), item.getItemtype());

        assertEquals("Miami Heat at Philadelphia 76ers - Game 3 (Home Game 1)", item.get("name").toString().strip());

        Item location = (Item) item.get("location");
        assertNotNull(location);
        assertEquals(List.of(new URI("http://schema.org/Place")), location.getItemtype());
        assertEquals(new URI("wells-fargo-center.html"), location.get("url"));

        Item address = (Item) location.get("address");
        assertNotNull(address);
        assertEquals(List.of(new URI("http://schema.org/PostalAddress")), address.getItemtype());
        assertEquals("Philadelphia", address.get("addressLocality"));

        String json = item.toJson();
        Item gsonItem = Item.fromJson(json);
        assertEquals("Miami Heat at Philadelphia 76ers - Game 3 (Home Game 1)", ((List<?>)gsonItem.getProperties().get("name")).get(0).toString().strip());
        assertEquals(List.of("http://schema.org/Event"), gsonItem.getItemtype());
        assertEquals(List.of("nba-miami-philidelphia-game3.html"), gsonItem.getProperties().get("url"));

        Item gsonLocation = (Item) gsonItem.getProperties().get("location").get(0);
        assertTrue(gsonLocation instanceof Item);
        assertEquals("wells-fargo-center.html", gsonLocation.getProperties().get("url").get(0).toString());

        Item gsonAddress = (Item) gsonLocation.getProperties().get("address").get(0);
        assertTrue(gsonAddress instanceof Item);
        assertEquals("Philadelphia", gsonAddress.getProperties().get("addressLocality").get(0));
    }

    @Test
    public void testParseUnlinked() throws IOException, URISyntaxException {
        // 通过类路径加载文件
        ClassLoader classLoader = getClass().getClassLoader();
        URI uri = classLoader.getResource("test-data/unlinked.html").toURI();
        List<Item> items = Microdata.getItems(uri.getPath());

        assertEquals(2, items.size());

        Item item = items.get(0);
        assertEquals(List.of(new URI("http://schema.org/Person")), item.getItemtype());
        assertEquals("Jane Doe", item.get("name"));
        assertNull(item.get("streetAddress"));

        Item postalAddress = items.get(1);
        assertEquals(List.of(new URI("http://schema.org/PostalAddress")), postalAddress.getItemtype());
        assertTrue(postalAddress.get("streetAddress").toString().contains("Whitworth"));
    }

    @Test
    public void testSkipLevel() throws IOException, URISyntaxException {
        // 通过类路径加载文件
        ClassLoader classLoader = getClass().getClassLoader();
        URI uri = classLoader.getResource("test-data/skip-level.html").toURI();
        List<Item> items = Microdata.getItems(uri.getPath());

        assertEquals(1, items.size());
        assertEquals("Jane Doe", items.get(0).get("name"));
    }

    @Test
    public void testParseMultipleProps() throws IOException, URISyntaxException {
        // 通过类路径加载文件
        ClassLoader classLoader = getClass().getClassLoader();
        URI uri = classLoader.getResource("test-data/multiple-props.html").toURI();
        List<Item> items = Microdata.getItems(uri.getPath());

        assertEquals(2, items.size());

        Item item = items.get(0);
        String json = item.toJson();
        Map<String, Object> i = new Gson().fromJson(json, Map.class);

        assertEquals(2, ((List<?>) ((Map<?, ?>) i.get("properties")).get("author")).size());
        assertEquals(List.of("John Doe", "Jane Dun"), ((List<?>) ((Map<?, ?>) ((Map<?, ?>) ((List<?>) ((Map<?, ?>) i.get("properties")).get("author")).get(0)).get("properties")).get("name")));
        assertEquals(2, ((List<?>) ((Map<?, ?>) i.get("properties")).get("creator")).size());
        assertEquals(List.of("John Doe", "Jane Dun"), ((List<?>) ((Map<?, ?>) ((Map<?, ?>) ((List<?>) ((Map<?, ?>) i.get("properties")).get("creator")).get(0)).get("properties")).get("name")));

        Item author = (Item) item.get("author");
        assertEquals("Stanford University", ((Item) author.get("affiliation")).get("name"));
        
        Item creator = (Item) item.get("creator");
        assertEquals("Stanford University", ((Item) creator.get("affiliation")).get("name"));
        
        assertEquals("Stanford University", ((Item) author.get("alumniOf")).get("name"));
        assertEquals("Stanford University", ((Item) creator.get("alumniOf")).get("name"));

        Item secondItem = items.get(1);
        String secondJson = secondItem.toJson();
        Map<String, Object> secondI = new Gson().fromJson(secondJson, Map.class);

        assertEquals("orange", ((List<?>) ((Map<?, ?>) secondI.get("properties")).get("favorite-color")).get(0));
        assertEquals("orange", ((List<?>) ((Map<?, ?>) secondI.get("properties")).get("favorite-fruit")).get(0));
    }
}
