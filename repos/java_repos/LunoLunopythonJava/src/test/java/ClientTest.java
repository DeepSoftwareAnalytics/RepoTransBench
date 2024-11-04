import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper; // Ensure this import

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientTest {
    private Client client;
    private OkHttpClient mockHttpClient;
    private Call mockCall;
    private Response mockResponse;

    @BeforeEach
    public void setUp() {
        client = new Client();
        client.setAuth("api_key_id", "api_key_secret");
        client.setBaseUrl("base_url");
        client.setTimeout(10);
        mockHttpClient = mock(OkHttpClient.class);
        mockCall = mock(Call.class);
        mockResponse = mock(Response.class);
    }

    @Test
    public void testClientInitialization() {
        assertEquals("api_key_id", client.getApiKeyId());
        assertEquals("api_key_secret", client.getApiKeySecret());
        assertEquals("base_url", client.getBaseUrl());
        assertEquals(10, client.getTimeout());
    }

    @Test
    public void testClientDoBasic() throws IOException, APIError {
        client.setBaseUrl("mock://test/");

        try (MockedStatic<OkHttpClient> mocked = Mockito.mockStatic(OkHttpClient.class, invocation -> mockHttpClient)) {
            when(mockHttpClient.newCall(any())).thenReturn(mockCall);
            when(mockCall.execute()).thenReturn(mockResponse);

            // Mock a response for ok
            Request mockRequest = new Request.Builder().url("mock://test/").build();
            when(mockResponse.request()).thenReturn(mockRequest);
            when(mockResponse.isSuccessful()).thenReturn(true);
            when(mockResponse.body()).thenReturn(ResponseBody.create("ok", MediaType.get("application/json")));

            assertThrows(Exception.class, () -> {
                Map<String, Object> res = client.doRequest("GET", "/", null, true);
            });

            // Mock a response for {"key":"value"}
            when(mockResponse.body()).thenReturn(ResponseBody.create("{\"key\":\"value\"}", MediaType.get("application/json")));

            Map<String, Object> res = client.doRequest("GET", "/", null, false);
            assertEquals("value", res.get("key"));

            // Mock a response for 400 status code with no error
            when(mockResponse.code()).thenReturn(400);
            when(mockResponse.body()).thenReturn(ResponseBody.create("{}", MediaType.get("application/json")));

            res = client.doRequest("GET", "/", null, false); // no exception, because no error present

            // Mock a response for {"error_code":"code","error":"message"} with 400 status code
            when(mockResponse.code()).thenReturn(400);
            when(mockResponse.body()).thenReturn(ResponseBody.create("{\"error_code\":\"code\",\"error\":\"message\"}", MediaType.get("application/json")));

            APIError apiError = assertThrows(APIError.class, () -> {
                client.doRequest("GET", "/", null, false);
            });

            assertEquals("code", apiError.getCode());
            assertEquals("message", apiError.getMessage());
        }
    }
}

