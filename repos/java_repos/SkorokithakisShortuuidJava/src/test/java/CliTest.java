import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shortuuid.Cli;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CliTest {

    @Test
    void testShortUUIDCommandProducesUUID() {
        Cli cliMock = mock(Cli.class);
        doNothing().when(cliMock).main(any(String[].class));

        Cli.main(new String[]{});

        verify(cliMock, times(1)).main(new String[]{});
    }

    @Test
    void testEncodeCommand() {
        Cli cliMock = mock(Cli.class);
        doNothing().when(cliMock).main(any(String[].class));

        Cli.main(new String[]{"encode", "3b1f8b40-222c-4a6e-b77e-779d5a94e21c"});

        verify(cliMock, times(1)).main(new String[]{"encode", "3b1f8b40-222c-4a6e-b77e-779d5a94e21c"});
    }

    @Test
    void testDecodeCommand() {
        Cli cliMock = mock(Cli.class);
        doNothing().when(cliMock).main(any(String[].class));

        Cli.main(new String[]{"decode", "CXc85b4rqinB7s5J52TRYb"});

        verify(cliMock, times(1)).main(new String[]{"decode", "CXc85b4rqinB7s5J52TRYb"});
    }
}
