import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

public class Ksuid {
    private static final int TIMESTAMP_LENGTH = 4;
    private static final int PAYLOAD_LENGTH = 16;
    private static final int EPOCH_TIME = 1400000000;
    private final byte[] uid;

    public Ksuid(Long timestamp) {
        SecureRandom random = new SecureRandom();
        byte[] payload = new byte[PAYLOAD_LENGTH];
        random.nextBytes(payload);

        long currTime = timestamp != null ? timestamp : Instant.now().getEpochSecond();
        byte[] timeBytes = ByteBuffer.allocate(4).putInt((int) (currTime - EPOCH_TIME)).array();

        uid = new byte[PAYLOAD_LENGTH + TIMESTAMP_LENGTH];
        System.arraycopy(timeBytes, 0, uid, 0, TIMESTAMP_LENGTH);
        System.arraycopy(payload, 0, uid, TIMESTAMP_LENGTH, PAYLOAD_LENGTH);
    }

    public Ksuid() {
        this(null);
    }

    public LocalDateTime getDatetime() {
        return LocalDateTime.ofEpochSecond(getTimestamp(), 0, ZoneOffset.UTC);
    }

    public long getTimestamp() {
        ByteBuffer buffer = ByteBuffer.wrap(uid, 0, TIMESTAMP_LENGTH);
        long timestamp = buffer.getInt() & 0xFFFFFFFFL;
        return timestamp + EPOCH_TIME;
    }

    public byte[] getPayload() {
        byte[] payload = new byte[PAYLOAD_LENGTH];
        System.arraycopy(uid, TIMESTAMP_LENGTH, payload, 0, PAYLOAD_LENGTH);
        return payload;
    }

    public byte[] toBytes() {
        return uid;
    }

    public static Ksuid fromBytes(byte[] bytes) {
        if (bytes.length != TIMESTAMP_LENGTH + PAYLOAD_LENGTH) {
            throw new IllegalArgumentException("Invalid byte array length");
        }

        Ksuid ksuid = new Ksuid();
        System.arraycopy(bytes, 0, ksuid.uid, 0, bytes.length);
        return ksuid;
    }

    public String toBase62() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uid);
    }

    public static Ksuid fromBase62(String data) {
        byte[] bytes = Base64.getUrlDecoder().decode(data);
        return fromBytes(bytes);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (byte b : uid) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

