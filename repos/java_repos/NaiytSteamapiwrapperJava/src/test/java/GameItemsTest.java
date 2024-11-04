import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class GameItemsTest {

    private GameItems gameItems;
    private static final String API_KEY = "dummy_api_key";

    @BeforeEach
    public void setUp() {
        gameItems = new GameItems(API_KEY);
    }

    @Test
    public void testGetAllTf2() {
        SteamAPI apiSpy = Mockito.spy(gameItems);
        Mockito.doReturn(getMockedResponse()).when(apiSpy).getJson(anyString());

        Map<String, Map<String, Object>> items = gameItems.getAll("tf2", false);
        assertNotNull(items);
        assertTrue(items.containsKey("item1"));
        assertEquals(123, items.get("item1").get("defindex"));
    }

    private String getMockedResponse() {
        return "{ \"result\": { \"items\": [{ \"name\": \"item1\", \"defindex\": 123, \"item_class\": \"class1\", \"item_type_name\": \"type1\" }] } }";
    }
}
