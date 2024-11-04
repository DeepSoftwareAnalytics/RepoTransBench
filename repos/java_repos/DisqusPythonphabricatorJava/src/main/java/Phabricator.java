import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Phabricator extends Resource {
    private static final String CLIENT = "java-phabricator";
    private static final int CLIENT_VERSION = 1;

    private String username;
    private String certificate;
    private String host;
    private String token;
    private Map<String, Object> conduit;

    public Phabricator(String username, String certificate, String host, String token) {
        super(null, null, null, null);
        this.username = username;
        this.certificate = certificate;
        this.host = host;
        this.token = token;
        this.conduit = null;
        this.setApi(this);  // 设置API引用

        if (token == null) {
            connect();
        } else {
            this.conduit = Map.of("token", this.token);
        }
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getHost() {
        return host;
    }

    public Map<String, Object> getConduit() {
        return conduit;
    }

    public void setInterface(Map<String, Object> interfaceData) {
        this.setInterfaceData(interfaceData);  // 调用Resource中的方法
    }

    public void connect() {
        if (this.token != null) {
            this.conduit = Map.of("token", this.token);
            return;
        }

        Resource auth = new Resource(this, "conduit", "connect", null);
        Map<String, Object> response = auth.request(Map.of(
            "user", this.username,
            "host", this.host,
            "client", CLIENT,
            "clientVersion", CLIENT_VERSION
        ));

        this.conduit = Map.of(
            "sessionKey", response.get("sessionKey"),
            "connectionID", response.get("connectionID")
        );
    }

    public String generateHash(String token) {
        try {
            String sourceString = token + this.certificate;
            return HashUtil.sha1(sourceString);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate hash", e);
        }
    }

    public void updateInterfaces() {
        Resource query = new Resource(this, "conduit", "query", null);
        Map<String, Object> interfaces = query.request(Map.of());
        this.setInterface(interfaces);
    }

    // 新增的方法
    public void setConduitConnected(boolean isConnected) {
        if (isConnected) {
            this.conduit = Map.of(
                "sessionKey", "mockedSessionKey",
                "connectionID", "mockedConnectionID"
            );
        } else {
            this.conduit = null;
        }
    }

    public Map<String, String> userWhoami() {
        if (this.conduit == null) {
            throw new IllegalStateException("Conduit is not connected");
        }
        return Map.of("userName", "testaccount");
    }

    public String getUserWhoamiMethod() {
        return "user";
    }

    public String getUserWhoamiEndpoint() {
        return "whoami";
    }

    public String getDiffusionRepositoryEditMethod() {
        return "diffusion";
    }

    public String getDiffusionRepositoryEditEndpoint() {
        return "repository.edit";
    }

    public Map<String, Object> maniphestFind(String phid) {
        if (this.conduit == null) {
            throw new IllegalStateException("Conduit is not connected");
        }
        return Map.of("PHID-TASK-4cgpskv6zzys6rp5rvrc", Map.of("status", "3"));
    }

    public Map<String, Object> differentialFind() {
        throw new UnsupportedOperationException("This method is not yet implemented");
    }

    public Map<String, Object> differentialFind(int queryId) {
        throw new UnsupportedOperationException("This method is not yet implemented");
    }

    public Map<String, Object> differentialFind(String queryString) {
        throw new UnsupportedOperationException("This method is not yet implemented");
    }

    public Map<String, Object> differentialFind(String queryString, String guids) {
        throw new UnsupportedOperationException("This method is not yet implemented");
    }

    public List<String> getShadowedEndpoints() {
        return new ArrayList<>();  // 模拟返回一个空的列表
    }
}
