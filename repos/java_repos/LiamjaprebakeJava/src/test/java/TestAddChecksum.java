import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TestAddChecksum {

    private String sampleData;
    private String sampleDataWithChecksum;
    private AddChecksum addChecksum;

    @BeforeEach
    public void setUp() {
        addChecksum = new AddChecksum();
        sampleData = "! Title: Example filter list\n"
                + "! Expires: 1 day\n"
                + "! Version: 20240622.1\n"
                + "! Last modified: 23 Jun 2024 12:34 UTC\n"
                + "\n"
                + "example.com###ad\n"
                + "example.com###banner\n";

        sampleDataWithChecksum = "! Title: Example filter list\n"
                + "! Expires: 1 day\n"
                + "! Version: 20240622.1\n"
                + "! Last modified: 23 Jun 2024 12:34 UTC\n"
                + "! Checksum: 2jmj7l5rSw0yVb/vlWAYkK/YBwk=\n"
                + "\n"
                + "example.com###ad\n"
                + "example.com###banner\n";
    }

    @Test
    public void testCalculateChecksum() throws NoSuchAlgorithmException {
        String normalizedData = addChecksum.normalize(sampleData);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(normalizedData.getBytes());
        String expectedChecksum = Base64.getEncoder().encodeToString(md5.digest()).replace("=", "");

        String checksum = addChecksum.calculateChecksum(sampleData);
        assertEquals(checksum, expectedChecksum);
    }

    @Test
    public void testAddChecksum() throws NoSuchAlgorithmException {
        String dataWithChecksum = addChecksum.addChecksum(sampleData);

        // Extract checksum from the returned data
        Pattern pattern = Pattern.compile("!\\s*Checksum[\\s\\-:]+([\\w\\+/=]+)");
        Matcher matcher = pattern.matcher(dataWithChecksum);
        assertNotNull(matcher.find(), "Checksum not found in the data");

        String extractedChecksum = matcher.group(1);

        // Recalculate checksum for the data without the checksum line
        String dataWithoutChecksum = dataWithChecksum.replaceAll("!\\s*Checksum[\\s\\-:]+([\\w\\+/=]+).*\n", "");
        String calculatedChecksum = addChecksum.calculateChecksum(dataWithoutChecksum);

        assertEquals(extractedChecksum, calculatedChecksum);
    }

    @Test
    public void testNormalize() {
        String dataWithExtraNewlines = "! Title: Example filter list\r\n"
                + "! Expires: 1 day\n\n"
                + "! Version: 20240622.1\n"
                + "! Last modified: 23 Jun 2024 12:34 UTC\r\n\r\n"
                + "example.com###ad\n\n"
                + "example.com###banner\n";

        String expectedNormalizedData = "! Title: Example filter list\n"
                + "! Expires: 1 day\n"
                + "! Version: 20240622.1\n"
                + "! Last modified: 23 Jun 2024 12:34 UTC\n"
                + "example.com###ad\n"
                + "example.com###banner\n";

        String normalizedData = addChecksum.normalize(dataWithExtraNewlines);
        assertEquals(normalizedData, expectedNormalizedData);
    }
}
