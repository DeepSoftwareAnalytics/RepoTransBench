import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Map {
    private static final int MAX_URL_LEN = 8192; // Maximum URL length.

    // Fields moved to Map for inheritance
    protected final int sizeX;
    protected final int sizeY;
    protected final String mapType;
    protected final Integer zoom;
    protected final int scale;
    protected final String key;
    protected final String language;
    protected final Object styles;
    protected final String clientId;
    protected final String secret;
    protected final String channel;

    public Map(int sizeX, int sizeY, String mapType, Integer zoom, int scale, String key, String language, Object styles, String clientId, String secret, String channel) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.mapType = mapType;
        this.zoom = zoom;
        this.scale = scale;
        this.key = key;
        this.language = language;
        this.styles = styles;
        this.clientId = clientId;
        this.secret = secret;
        this.channel = channel;
    }

    protected abstract String generateUrl();

    protected String sign(String url) throws SignatureException {
        try {
            byte[] secretKey = Base64.getDecoder().decode(secret);  // pass the secret field here
            MessageDigest sha1 = MessageDigest.getInstance("HmacSHA1");
            sha1.update(secretKey);
            return new String(Base64.getEncoder().encodeToString(sha1.digest(url.getBytes())));
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
    }

    protected void checkUrlLength(String url) {
        if (url.length() > MAX_URL_LEN) {
            throw new IllegalArgumentException(
                String.format("Generated URL is %d characters in length. Maximum is %d", url.length(), MAX_URL_LEN)
            );
        }
    }
}
