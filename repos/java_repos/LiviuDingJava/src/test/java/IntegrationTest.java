import org.junit.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntegrationTest {

    private String runTool(String... args) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            boolean finished = process.waitFor(3, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
            }
            return result.toString();
        }
    }

    @Test
    public void testIntegrationIn1Second() throws IOException, InterruptedException {
        runTool("java", "-cp", "target/classes", "Ding", "in", "1s");
    }

    @Test
    public void testIntegrationWithNoTimer() throws IOException, InterruptedException {
        String output = runTool("java", "-cp", "target/classes", "Ding", "in", "1s", "--no-timer");
        assertEquals("", output);
    }
}
