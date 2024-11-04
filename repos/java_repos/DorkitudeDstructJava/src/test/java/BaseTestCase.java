import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;

public class BaseTestCase {

    @BeforeAll
    public static void setUpClass() {
        set_up_class();
    }

    @AfterAll
    public static void tearDownClass() {
        tear_down_class();
    }

    @BeforeEach
    public void setUp() {
        set_up();
    }

    @AfterEach
    public void tearDown() {
        tear_down();
    }

    public static void set_up_class() {
        // Override in subclasses for class-level setup
    }

    public static void tear_down_class() {
        // Override in subclasses for class-level teardown
    }

    public void set_up() {
        // Override in subclasses for test-by-test setup
    }

    public void tear_down() {
        // Override in subclasses for test-by-test teardown
    }

    // Snake_case to MixedCase conversion utility
    public static String snakeToMixed(String name) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    // Dynamic method aliasing for snake_case to mixedCase
    public Object __getattr__(String name) throws Exception {
        String mixedName = snakeToMixed(name);
        try {
            return this.getClass().getMethod(mixedName).invoke(this);
        } catch (NoSuchMethodException e) {
            return this.getClass().getMethod(name).invoke(this);
        }
    }
}
