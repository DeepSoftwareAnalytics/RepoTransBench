import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SteamUserTest {

    private SteamUser user;
    private static final String API_KEY = "dummy_api_key";
    private static final String STEAM_ID = "dummy_steam_id";

    @BeforeEach
    public void setUp() {
        user = new SteamUser(STEAM_ID, API_KEY);
    }

    @Test
    public void testGetGames() {
        SteamUser userSpy = spy(user);
        doReturn(getMockedGamesResponse()).when(userSpy).getResponse(anyString());

        List<Map<String, Object>> games = userSpy.getGames();
        assertEquals(1, games.size());
        assertEquals(123, games.get(0).get("appid"));
        assertEquals("Game1", games.get(0).get("name"));
    }

    private String getMockedGamesResponse() {
        return "{\"response\": { \"games\": [ { \"appid\": 123, \"name\": \"Game1\" }] }}";
    }

    @Test
    public void testGetItems() {
        SteamUser userSpy = spy(user);
        doReturn(getMockedItemsResponse()).when(userSpy).getResponse(anyString());

        Map<Integer, Map<String, Object>> items = userSpy.getItems("tf2", false);
        assertNotNull(items);
        assertTrue(items.containsKey(123));
        assertEquals(10, items.get(123).get("level"));
    }

    private String getMockedItemsResponse() {
        return "{\"result\": { \"status\": 1, \"items\": [ { \"id\": 123, \"level\": 10, \"quality\": 5, \"quantity\": 1 }] }}";
    }
}
