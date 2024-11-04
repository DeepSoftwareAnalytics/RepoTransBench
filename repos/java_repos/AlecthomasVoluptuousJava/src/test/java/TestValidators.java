import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestValidators {
    @Test
    public void testMatch() {
        Validators.Match validator = new Validators.Match("\\d+");
        try {
            assertEquals("123", validator.validate("123"));
        } catch (Invalid e) {
            fail("Exception should not have been thrown");
        }
        assertThrows(Invalid.class, () -> validator.validate("abc"));
    }

    @Test
    public void testRange() {
        Validators.Range validator = new Validators.Range(1, 10);
        try {
            assertEquals(5, validator.validate(5));
        } catch (Invalid e) {
            fail("Exception should not have been thrown");
        }
        assertThrows(Invalid.class, () -> validator.validate(11));
    }

    // Other validator tests can be added similarly
}

