import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestHaaska {
    private Configuration configuration;
    private HomeAssistant homeAssistant;

    @BeforeEach
    public void setUp() {
        Map<String, Object> optsDict = new HashMap<>();
        optsDict.put("url", "http://localhost:8123");
        optsDict.put("bearer_token", "");
        optsDict.put("debug", false);
        optsDict.put("ssl_verify", true);
        optsDict.put("ssl_client", new String[]{});

        configuration = new Configuration(optsDict);
        homeAssistant = new HomeAssistant(configuration);
    }

    @Test
    public void testHaBuildUrl() {
        String url = homeAssistant.buildUrl("test");
        assertEquals("http://localhost:8123/api/test", url);
    }

    @Test
    public void testGetUserAgent() {
        try (MockedStatic<System> mocked = Mockito.mockStatic(System.class)) {
            mocked.when(() -> System.getenv("AWS_DEFAULT_REGION")).thenReturn("test");
            String userAgent = homeAssistant.getUserAgent();
            assertTrue(userAgent.startsWith("Home Assistant Alexa Smart Home Skill - test - java-httpclient"));
        }
    }

    @Test
    public void testConfigGet() {
        assertFalse((Boolean) configuration.get(new String[]{"debug"}));
        assertNull(configuration.get(new String[]{"test"}));
        assertEquals("default", configuration.get(new String[]{"test"}, "default"));
    }

    @Test
    public void testConfigGetUrl() {
        String[] testUrls = {
            "http://hass.example.com:8123",
            "http://hass.example.app"
        };
        for (String expectedUrl : testUrls) {
            assertEquals(expectedUrl, configuration.getUrl(expectedUrl + "/"));
            assertEquals(expectedUrl, configuration.getUrl(expectedUrl + "/api"));
            assertEquals(expectedUrl, configuration.getUrl(expectedUrl + "/api/"));
        }
    }
}
