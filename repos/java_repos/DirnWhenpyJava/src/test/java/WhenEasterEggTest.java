import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.LocalDateTime;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class WhenEasterEggTest {

    @Test
    public void test_is_5_oclock() {
        // Mock When class
        When mockWhen = Mockito.mock(When.class);

        // Test that it's before 5 o'clock
        Mockito.when(mockWhen.now()).thenReturn(LocalDateTime.of(2012, 9, 3, 16, 0));
        Duration countdown = mockWhen.is5Oclock();
        assertTrue(countdown.getSeconds() >= 0);  // The countdown should be non-negative if it's before 5 PM

        // Test that it *is* 5 o'clock
        Mockito.when(mockWhen.now()).thenReturn(LocalDateTime.of(2012, 9, 3, 17, 0));
        countdown = mockWhen.is5Oclock();
        assertEquals(0, countdown.getDays());     // Ensure no days difference
        assertEquals(0, countdown.getSeconds());  // Ensure no seconds difference
        assertEquals(0, countdown.getNano());     // Ensure no nanoseconds difference

        // Test that it's after 5 o'clock
        Mockito.when(mockWhen.now()).thenReturn(LocalDateTime.of(2012, 9, 3, 18, 0));
        countdown = mockWhen.is5Oclock();
        assertTrue(countdown.getSeconds() < 0);  // The countdown should be negative after 5 PM
    }
}
