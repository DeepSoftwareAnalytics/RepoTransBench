import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class VarDumpTests {

    @Test
    public void test_var_dump_single_value() {
        // Mock System.out.println using Mockito
        try (MockedStatic<System> mockedStatic = Mockito.mockStatic(System.class)) {
            VarDump.var_dump("abc");

            // Verify that System.out.println was called with the expected value
            mockedStatic.verify(() -> System.out.println("#0 str(3) \"abc\""), times(1));
        }
    }

    @Test
    public void test_var_dump_multiple_values_at_once() {
        // Mock System.out.println using Mockito
        try (MockedStatic<System> mockedStatic = Mockito.mockStatic(System.class)) {
            VarDump.var_dump("foo", 55, false);

            // Verify that System.out.println was called with the expected values
            mockedStatic.verify(() -> System.out.println("#0 str(3) \"foo\""), times(1));
            mockedStatic.verify(() -> System.out.println("#1 int(55) "), times(1));
            mockedStatic.verify(() -> System.out.println("#2 bool(False) "), times(1));
        }
    }
}
