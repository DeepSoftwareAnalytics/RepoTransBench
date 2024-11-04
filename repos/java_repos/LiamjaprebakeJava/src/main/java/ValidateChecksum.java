import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateChecksum {

    private static final Pattern CHECKSUM_PATTERN = Pattern.compile("^\\s*!\\s*checksum[\\s\\-:]+([\\w\\+/=]+).*\n", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public void validate(String data) throws Exception {
        String checksum = extractChecksum(data);
        if (checksum == null) {
            throw new Exception("Data doesn't contain a checksum, nothing to validate");
        }

        String dataWithoutChecksum = data.replaceAll("!\\s*Checksum[\\s\\-:]+([\\w\\+/=]+).*\n", ""); // Remove the checksum line
        String expectedChecksum = calculateChecksum(dataWithoutChecksum);
        if (checksum.equals(expectedChecksum)) {
            System.out.println("Checksum is valid");
        } else {
            System.out.printf("Wrong checksum: found %s, expected %s\n", checksum, expectedChecksum);
        }
    }

    public String extractChecksum(String data) {
        Matcher matcher = CHECKSUM_PATTERN.matcher(data);
        return matcher.find() ? matcher.group(1) : null;
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
