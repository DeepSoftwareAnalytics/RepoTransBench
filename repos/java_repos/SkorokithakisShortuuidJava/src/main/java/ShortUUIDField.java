package shortuuid;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShortUUIDField {
    private int length;
    private String prefix;
    private String alphabet;

    public ShortUUIDField() {
        this.length = 22; // default
        this.prefix = "";

        // initialize other fields
        this.alphabet = new ShortUUID().getAlphabet();
    }

    public ShortUUIDField(int length, String prefix, String alphabet) {
        this.length = length;
        this.prefix = prefix;
        this.alphabet = alphabet;
    }

    private String generateUUID() {
        return prefix + new ShortUUID(alphabet).random(length);
    }

    public void deconstruct(Map<String, Object> kwargs) {
        kwargs.put("alphabet", alphabet);
        kwargs.put("length", length);
        kwargs.put("prefix", prefix);
    }
}
