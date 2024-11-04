import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class BeepTester {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    public void captureOutput() {
        System.setOut(new PrintStream(outputStream));
    }

    public String getOutput() {
        System.setOut(originalOut);
        return outputStream.toString();
    }
}
