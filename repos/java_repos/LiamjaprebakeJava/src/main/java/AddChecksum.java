import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddChecksum {

    private static final Pattern CHECKSUM_PATTERN = Pattern.compile("^\\s*!\\s*checksum[\\s\\-:]+([\\w\\+/=]+).*\n", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public String addChecksum(String data) throws NoSuchAlgorithmException {
        data = normalize(data); // Step 1: Normalize the data first
        String checksum = calculateChecksum(data);
        data = CHECKSUM_PATTERN.matcher(data).replaceAll(""); // Remove any existing checksum line
        data = data + "! Checksum: " + checksum + "\n"; // Add the checksum line at the end of the data
        return data;
    }

    public String calculateChecksum(String data) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(normalize(data).getBytes());
        return Base64.getEncoder().encodeToString(md5.digest()).replace("=", "");
    }

    public String normalize(String data) {
        data = data.replaceAll("\r", ""); // Remove all \r characters
        data = data.replaceAll("\n+", "\n"); // Reduce multiple consecutive newlines to a single newline
        data = data.trim() + "\n"; // Ensure the data ends with a single newline
        data = CHECKSUM_PATTERN.matcher(data).replaceAll(""); // Remove any existing checksum lines
        return data;
    }
}
