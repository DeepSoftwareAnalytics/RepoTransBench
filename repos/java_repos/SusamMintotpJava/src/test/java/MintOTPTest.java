import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Scanner;

public class MintOTPTest {

    private final String SECRET1 = "ZYTYYE5FOAGW5ML7LRWUL4WTZLNJAMZS";
    private final String SECRET2 = "PW4YAYYZVDE5RK2AOLKUATNZIKAFQLZO";

    @Test
    public void testHotp() {
        assertEquals("549419", MintOTP.hotp(SECRET1, 0));
        assertEquals("009551", MintOTP.hotp(SECRET2, 0));
        assertEquals("626854", MintOTP.hotp(SECRET1, 42));
        assertEquals("093610", MintOTP.hotp(SECRET2, 42));
    }

    @Test
    public void testTotp() {
        try (MockedStatic<System> system = Mockito.mockStatic(System.class)) {
            system.when(System::currentTimeMillis).thenReturn(0L);
            assertEquals("549419", MintOTP.totp(SECRET1));
            assertEquals("009551", MintOTP.totp(SECRET2));

            system.when(System::currentTimeMillis).thenReturn(10L * 1000);
            assertEquals("549419", MintOTP.totp(SECRET1));
            assertEquals("009551", MintOTP.totp(SECRET2));

            system.when(System::currentTimeMillis).thenReturn(1260L * 1000);
            assertEquals("626854", MintOTP.totp(SECRET1));
            assertEquals("093610", MintOTP.totp(SECRET2));

            system.when(System::currentTimeMillis).thenReturn(1270L * 1000);
            assertEquals("626854", MintOTP.totp(SECRET1));
            assertEquals("093610", MintOTP.totp(SECRET2));
        }
    }

    @Test
    public void testMainOneSecret() {
        try (MockedStatic<System> system = Mockito.mockStatic(System.class);
             MockedStatic<MintOTP> mockMintOTP = Mockito.mockStatic(MintOTP.class);
             Scanner scanner = new Scanner("ZYTYYE5FOAGW5ML7LRWUL4WTZLNJAMZS")){

            system.when(System::currentTimeMillis).thenReturn(0L);
            MintOTP.main(new String[]{});
            mockMintOTP.verify(() -> MintOTP.totp(SECRET1, 30, 6, "HmacSHA1"), times(1));
        }
    }

    @Test
    public void testMainTwoSecrets() {
        try (MockedStatic<System> system = Mockito.mockStatic(System.class);
             MockedStatic<MintOTP> mockMintOTP = Mockito.mockStatic(MintOTP.class);
             Scanner scanner = new Scanner("ZYTYYE5FOAGW5ML7LRWUL4WTZLNJAMZS\nPW4YAYYZVDE5RK2AOLKUATNZIKAFQLZO")) {

            system.when(System::currentTimeMillis).thenReturn(0L);
            MintOTP.main(new String[]{});
            mockMintOTP.verify(() -> MintOTP.totp(SECRET1, 30, 6, "HmacSHA1"), times(1));
            mockMintOTP.verify(() -> MintOTP.totp(SECRET2, 30, 6, "HmacSHA1"), times(1));
        }
    }

    @Test
    public void testMainStep() {
        try (MockedStatic<System> system = Mockito.mockStatic(System.class);
             MockedStatic<MintOTP> mockMintOTP = Mockito.mockStatic(MintOTP.class);
             Scanner scanner = new Scanner("ZYTYYE5FOAGW5ML7LRWUL4WTZLNJAMZS")) {

            system.when(System::currentTimeMillis).thenReturn(2520L * 1000);
            MintOTP.main(new String[]{"60"});
            mockMintOTP.verify(() -> MintOTP.totp(SECRET1, 60, 6, "HmacSHA1"), times(1));
        }
    }

    @Test
    public void testMainDigits() {
        try (MockedStatic<System> system = Mockito.mockStatic(System.class);
             MockedStatic<MintOTP> mockMintOTP = Mockito.mockStatic(MintOTP.class);
             Scanner scanner = new Scanner("ZYTYYE5FOAGW5ML7LRWUL4WTZLNJAMZS")) {

            system.when(System::currentTimeMillis).thenReturn(0L);
            MintOTP.main(new String[]{"30", "8"});
            mockMintOTP.verify(() -> MintOTP.totp(SECRET1, 30, 8, "HmacSHA1"), times(1));
        }
    }

    @Test
    public void testMainDigest() {
        try (MockedStatic<System> system = Mockito.mockStatic(System.class);
             MockedStatic<MintOTP> mockMintOTP = Mockito.mockStatic(MintOTP.class);
             Scanner scanner = new Scanner("ZYTYYE5FOAGW5ML7LRWUL4WTZLNJAMZS")) {

            system.when(System::currentTimeMillis).thenReturn(0L);
            MintOTP.main(new String[]{"30", "6", "sha256"});
            mockMintOTP.verify(() -> MintOTP.totp(SECRET1, 30, 6, "HmacSHA256"), times(1));
        }
    }

    @Test
    public void testModule() {
        try (MockedStatic<System> system = Mockito.mockStatic(System.class);
             MockedStatic<MintOTP> mockMintOTP = Mockito.mockStatic(MintOTP.class);
             Scanner scanner = new Scanner("ZYTYYE5FOAGW5ML7LRWUL4WTZLNJAMZS")) {

            system.when(System::currentTimeMillis).thenReturn(0L);
            MintOTP.main(new String[]{});
            mockMintOTP.verify(() -> MintOTP.totp(SECRET1, 30, 6, "HmacSHA1"), times(1));
        }
    }
}
