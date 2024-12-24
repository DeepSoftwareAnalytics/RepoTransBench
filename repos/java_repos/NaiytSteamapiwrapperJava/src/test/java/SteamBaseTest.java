import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SteamBaseTest {

    private SteamAPI api;
    private static final String URL = "http://steampowered.com";

    @BeforeEach
    public void setUp() {
        api = new SteamAPI("steamid", "apikey");
    }

    @Test
    public void testGetJsonNoParams() {
        SteamAPI apiSpy = Mockito.spy(api);
        String mockedResponse = "{ \"name\": \"nate\" }";
        doReturn(mockedResponse).when(apiSpy).getResponse(URL);

        assertEquals("nate", apiSpy.getJson(URL).getString("name"));
    }

    @Test
    public void testDateConversion() {
        LocalDateTime dateTime = api.convertToLocalDateTime(1406396420);
        assertEquals("2014-07-26T20:20:20", dateTime.toString());
    }
}
