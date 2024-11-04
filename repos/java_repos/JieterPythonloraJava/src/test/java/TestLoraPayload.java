import lora.LoRaPayload;
import org.junit.Test;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class TestLoraPayload {

    private static final String FIXTURES_PATH = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "fixtures").toString();

    private String read(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename))).trim();
    }

    private List<Object[]> fixtures() throws IOException {
        try (Stream<Path> devicePaths = Files.list(Paths.get(FIXTURES_PATH))) {
            return devicePaths
                    .filter(Files::isDirectory)
                    .filter(path -> !path.getFileName().toString().equals("README.md"))
                    .flatMap(devicePath -> {
                        final String devAddr = devicePath.getFileName().toString(); // Make devAddr final
                        final String key;
                        try {
                            key = read(Paths.get(devicePath.toString(), "key.hex").toString()); // Make key final
                        } catch (IOException e) {
                            e.printStackTrace();
                            return Stream.empty();
                        }
    
                        try (Stream<Path> fixtureFiles = Files.list(devicePath)) {
                            return fixtureFiles
                                    .filter(path -> path.toString().endsWith(".xml"))
                                    .map(fixtureFile -> {
                                        final String fixtureFilename = fixtureFile.getFileName().toString(); // Make fixtureFilename final
                                        final String fixture = devicePath.getFileName().toString() + "/" + fixtureFilename; // Make fixture final
                                        final String expected;
                                        if (!fixtureFilename.contains("plaintext")) {
                                            try {
                                                expected = read(fixtureFile.toString().replace(".xml", ".txt")); // Make expected final
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                return null;
                                            }
                                        } else {
                                            expected = null;
                                        }
                                        try {
                                            return new Object[]{devAddr, key, fixture, read(fixtureFile.toString()), expected};
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            return null;
                                        }
                                    })
                                    .collect(Collectors.toList()).stream();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return Stream.empty();
                        }
                    }).collect(Collectors.toList());
        }
    }    

    @Test
    public void testXmlParsing() throws Exception {
        String xmlFilename = Paths.get(FIXTURES_PATH, "000015E4", "payload_1.xml").toString();
        LoRaPayload payload = new LoRaPayload(read(xmlFilename));

        assertEquals("1", payload.getAttribute("DevLrrCnt"));
        assertEquals("2", payload.getAttribute("FCntUp"));
        assertEquals("SRID=4326;POINT(4.36984 52.014877)", payload.getLrrLocation());
    }

    @Test
    public void testDecryptingPayload() throws Exception {
        for (Object[] fixture : fixtures()) {
            String devAddr = (String) fixture[0];
            String key = (String) fixture[1];
            String fixtureFilename = (String) fixture[2];
            String xml = (String) fixture[3];
            String expected = (String) fixture[4];

            LoRaPayload payload = new LoRaPayload(xml);
            byte[] plaintextInts = payload.decrypt(key, devAddr);

            // Convert decrypted byte array to hex string
            StringBuilder decryptedHex = new StringBuilder();
            for (byte b : plaintextInts) {
                decryptedHex.append(String.format("%02x", b));
            }

            assertEquals(
                "Decryption should not change length of hex string", 
                payload.getAttribute("payload_hex").length(), 
                decryptedHex.length()
            );

            if (expected == null) {
                // plaintext in filename, skip checking the expected outcome
                continue;
            }

            assertEquals(
                decryptedHex.toString(),
                expected,
                "Decrypted payload " + fixtureFilename + " not as expected"
            );
        }
    }
}
