import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.List;

// 简单的 Tuple 类实现
class Tuple<A, B> {
    public final A first;
    public final B second;

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }
}


public class PhabricatorTest {

    private Phabricator api;
    private WireMockServer wireMockServer;
    private String certificate;
    private Map<String, Object> responses;

    @BeforeEach
    public void setUp() throws IOException {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8080));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);

        certificate = new String(Files.readAllBytes(Paths.get("src/test/resources/certificate.txt")));
        String jsonResponse = new String(Files.readAllBytes(Paths.get("src/test/resources/responses.json")));
        responses = new Gson().fromJson(jsonResponse, Map.class);

        api = new Phabricator("test", "test", "http://localhost/api/", "test_token");
        api.setCertificate(certificate);
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testGenerateHash() {
        String token = "12345678";
        String hashed = api.generateHash(token);
        assertEquals("f8d3bea4e58a2b2967d93d5b307bfa7c693b2e7f", hashed);
    }

    @Test
    public void testConnect() {
        stubFor(post(urlEqualTo("/api/conduit.connect"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responses.get("conduit.connect").toString())));

        api.connect();
        assertNotNull(api.getConduit().get("sessionKey"));
        assertNotNull(api.getConduit().get("connectionID"));

        verify(postRequestedFor(urlEqualTo("/api/conduit.connect")));
    }

    @Test
    public void testUserWhoami() {
        stubFor(post(urlEqualTo("/api/user.whoami"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responses.get("user.whoami").toString())));

        api.setConduitConnected(true);
        assertEquals("testaccount", api.userWhoami().get("userName"));

        verify(postRequestedFor(urlEqualTo("/api/user.whoami")));
    }

    @Test
    public void testClassicResources() {
        assertEquals("user", api.getUserWhoamiMethod());
        assertEquals("whoami", api.getUserWhoamiEndpoint());
    }

    @Test
    public void testNestedResources() {
        assertEquals("diffusion", api.getDiffusionRepositoryEditMethod());
        assertEquals("repository.edit", api.getDiffusionRepositoryEditEndpoint());
    }

    @Test
    public void testBadStatus() {
        stubFor(post(urlEqualTo("/api/conduit.connect"))
                .willReturn(aResponse()
                        .withStatus(400)));

        api.setConduitConnected(true);

        assertThrows(IOException.class, () -> {
            api.userWhoami();
        });

        verify(postRequestedFor(urlEqualTo("/api/conduit.connect")));
    }

    @Test
    public void testManiphestFind() {
        stubFor(post(urlEqualTo("/api/maniphest.find"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responses.get("maniphest.find").toString())));

        api.setConduitConnected(true);
        Map<String, Object> result = api.maniphestFind("PHID-USER-5022a9389121884ab9db");

        assertEquals(1, result.size());
        assertTrue(result instanceof List);

        assertEquals("3", ((Map) result.get("PHID-TASK-4cgpskv6zzys6rp5rvrc")).get("status"));

        verify(postRequestedFor(urlEqualTo("/api/maniphest.find")));
    }

    @Test
    public void testValidation() {
        api.setConduitConnected(true);

        assertThrows(IllegalArgumentException.class, () -> {
            api.differentialFind();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            api.differentialFind(1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            api.differentialFind("1");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            api.differentialFind("1", "1");
        });
    }

    @Test
    public void testMapParamType() {
        assertEquals(Integer.class, Phabricator.mapParamType("uint"));

        assertEquals(List.class, Phabricator.mapParamType("list<bool>"));
        assertEquals(Tuple.class, Phabricator.mapParamType("list<pair<callsign, path>>"));

        assertEquals(Tuple.class, Phabricator.mapParamType("list<pair<string-constant<\"gtcm\">, string>>"));
    }

    @Test
    public void testEndpointShadowing() {
        List<String> shadowedEndpoints = api.getShadowedEndpoints();
        assertTrue(shadowedEndpoints.isEmpty(), "The following endpoints are shadowed: " + shadowedEndpoints);
    }
}
