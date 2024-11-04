package result;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ResultTest {

    @Test
    public void testPrintResults() throws Exception {
        byte[] fileBin = new byte[100];
        List<int[]> signatureRangeList = List.of(new int[]{0, 50});
        String outputFile = "output.txt";

        // Mock strings method
        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class)) {
            utilsMock.when(() -> Utils.strings(any(byte[].class), anyInt())).thenReturn(List.of("test", "string"));

            ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
            System.setOut(new PrintStream(consoleOutput));
            FileOutputStream fileOut = new FileOutputStream(outputFile);

            Result.printResults(fileBin, signatureRangeList, outputFile);

            // Validate file output content
            String fileContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(outputFile)));
            assertTrue(fileContent.contains("[+] Signature between bytes 0 and 50"));
            assertTrue(fileContent.contains("[+] Strings:"));
            assertTrue(fileContent.contains("> test"));
            assertTrue(fileContent.contains("> string"));

            // Clean up
            fileOut.close();
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(outputFile));
        }
    }
}
