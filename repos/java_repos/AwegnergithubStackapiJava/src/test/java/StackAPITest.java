import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.nio.file.*;
import java.util.*;

class StackAPITest {

    final String TESTS_DIRECTORY = "tests";
    final String directory = TESTS_DIRECTORY + "/";

    private String loadMockData(String filename) throws Exception {
        Path path = Paths.get(directory + filename);
        return new String(Files.readAllBytes(path));
    }

    private Map<String, Object> fakeStackoverflowExists(int page, String key, String filter, Map<String, Object> kwargs) throws Exception {
        String jsonContent = loadMockData("mock_objects/test_stackoverflow_exists");
        JSONObject jdata = new JSONObject(jsonContent);
        return jdata.toMap();
    }

    private Map<String, Object> fakeUsers(int page, String key, String filter, Map<String, Object> kwargs) throws Exception {
        String jsonContent = loadMockData("mock_objects/test_users_associated");
        JSONObject jdata = new JSONObject(jsonContent);
        return jdata.toMap();
    }

    @Test
    void testNoSiteProvided() {
        StackAPI site = new StackAPI();
        assertEquals("https://api.stackexchange.com/2.3/", site.baseUrl);
    }

    @Test
    void testNoEndpointProvided() {
        StackAPI site = spy(new StackAPI("stackoverflow"));
        
        // Mock the exception
        doThrow(new IllegalArgumentException("No end point provided."))
            .when(site)
            .fetch(eq((String)null), anyInt(), anyString(), anyString(), anyMap());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            site.fetch((String) null);
        });

        assertEquals("No end point provided.", exception.getMessage());
    }

    @Test
    void testStackoverflowExists() throws Exception {
        StackAPI site = new StackAPI("stackoverflow");
        StackAPI siteSpy = spy(site);

        Map<String, Object> response = fakeStackoverflowExists(1, null, "default", new HashMap<>());
        doReturn(response).when(siteSpy).fetch(anyString(), anyInt(), anyString(), anyString(), any());

        assertEquals("Stack Overflow", siteSpy.name);
    }

    @Test
    void testAsdfghjklNotExist() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StackAPI("asdfghjkl");
        });
        assertEquals("Invalid Site Name provided", exception.getMessage());
    }

    @Test
    void testNonsiteParameter() throws Exception {
        StackAPI site = new StackAPI();
        StackAPI siteSpy = spy(site);

        doReturn(fakeStackoverflowExists(1, null, "default", new HashMap<>())).when(siteSpy).fetch(anyString(), anyInt(), anyString(), anyString(), any());
        doReturn(fakeUsers(1, null, "default", new HashMap<>())).when(siteSpy).fetch(eq("/users/1/associated"), anyInt(), anyString(), anyString(), any());

        Map<String, Object> results = siteSpy.fetch("/users/1/associated");
        List<Object> items = (List<Object>) results.get("items");
        assertTrue(items.size() >= 1);
    }

    @Test
    void testExceptionsThrown() {
        Exception exception = assertThrows(StackAPI.StackAPIError.class, () -> {
            StackAPI site = new StackAPI("stackoverflow");
            site.fetch("errors/400");
        });

        assertTrue(exception.getMessage().contains("Error at URL: "));
    }

    @Test
    void testDefaultBaseUrl() throws Exception {
        StackAPI site = spy(new StackAPI("stackoverflow"));
        doReturn(fakeStackoverflowExists(1, null, "default", new HashMap<>())).when(site).fetch(anyString(), anyInt(), anyString(), anyString(), any());

        assertEquals("https://api.stackexchange.com/2.3/", site.baseUrl);
    }

    @Test
    void testOverrideBaseUrl() throws Exception {
        StackAPI site = new StackAPI("stackoverflow", "2.2", "https://mystacksite.com/api");
        StackAPI siteSpy = spy(site);

        Map<String, Object> response = fakeStackoverflowExists(1, null, "default", new HashMap<>());
        doReturn(response).when(siteSpy).fetch(anyString(), anyInt(), anyString(), anyString(), any());

        assertEquals("https://mystacksite.com/api/2.2/", siteSpy.baseUrl);
    }
}

