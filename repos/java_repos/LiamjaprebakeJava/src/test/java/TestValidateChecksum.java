import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TestValidateChecksum {

    private String sampleData;
    private String sampleDataWithChecksum;
    private ValidateChecksum validateChecksum;

    @BeforeEach
    public void setUp() {
        validateChecksum = new ValidateChecksum();
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
    public void testExtractChecksum() {
        String checksum = validateChecksum.extractChecksum(sampleDataWithChecksum);
        assertEquals(checksum, "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
    }

    @Test
    public void testCalculateChecksum() throws NoSuchAlgorithmException {
        String normalizedData = validateChecksum.normalize(sampleData);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(normalizedData.getBytes());
        String expectedChecksum = Base64.getEncoder().encodeToString(md5.digest()).replace("=", "");

        String checksum = validateChecksum.calculateChecksum(sampleData);
        assertEquals(checksum, expectedChecksum);
    }

    @Test
    public void testValidateValidChecksum() {
        try {
            validateChecksum.validate(sampleDataWithChecksum);
        } catch (Exception e) {
            fail("validate() raised " + e + " unexpectedly!");
        }
    }

    @Test
    public void testValidateNoChecksum() {
        Exception exception = assertThrows(Exception.class, () -> {
            validateChecksum.validate(sampleData);
        });
        assertEquals("Data doesn't contain a checksum, nothing to validate", exception.getMessage());
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

        String normalizedData = validateChecksum.normalize(dataWithExtraNewlines);
        assertEquals(normalizedData, expectedNormalizedData);
    }
}
