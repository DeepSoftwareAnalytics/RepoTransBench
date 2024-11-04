import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;

class CLITest {

    private int exit_0;
    private int exit_1;
    private int exit_2;
    private int exit_3;

    @BeforeEach
    void setUp() {
        /**
         * Defining the exitcodes
         */
        exit_0 = 0 << 8;
        exit_1 = 1 << 8;
        exit_2 = 2 << 8;
        exit_3 = 3 << 8;
    }

    @Test
    void testDebugPrint() {
        // Mocking System.out.print
        try (var mockPrintStream = Mockito.mockStatic(System.out)) {
            // Assuming debugPrint is a static method in a Debug class
            Debug.debugPrint(true, "debug");
            mockPrintStream.verify(() -> System.out.println("debug"), times(1));
        }
    }

    @Test
    void testVerbosePrint() {
        // Mocking System.out.print
        try (var mockPrintStream = Mockito.mockStatic(System.out)) {
            // Assuming verbosePrint is a static method in a Verbose class
            Verbose.verbosePrint(0, 3, "verbose");
            mockPrintStream.verifyNoInteractions();

            Verbose.verbosePrint(3, 3, "verbose");
            mockPrintStream.verify(() -> System.out.println("verbose"), times(1));
        }
    }

    @Test
    void testCliWithoutParams() {
        String command = "python3 check_http_json.py";
        int status = -1;
        try {
            Process process = Runtime.getRuntime().exec(command);
            status = process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(exit_2, status << 8);
    }
}
