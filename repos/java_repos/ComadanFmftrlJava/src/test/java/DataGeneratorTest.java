import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

public class DataGeneratorTest {

    @BeforeEach
    public void setUp() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("test_data.csv"))) {
            writer.write("id,hour,click,feature1,feature2\n");
            writer.write("1,14102100,1,A,B\n");
            writer.write("2,14102101,0,C,D\n");
        }
    }

    @AfterEach
    public void tearDown() {
        new File("test_data.csv").delete();
    }

    @Test
    public void testDataGenerator() throws IOException {
        DataGenerator generator = new DataGenerator("test_data.csv", 4194304, "salty");
        DataGenerator.Data data = generator.iterator().next();
        assertEquals(0, data.t);
        assertEquals(21, data.date);
        assertEquals("1", data.ID);
        assertEquals(1.0, data.y);
        assertEquals(3, data.x.size());
    }

    @Test
    public void testDataGeneratorCleanup() throws IOException {
        DataGenerator generator = new DataGenerator("test_data.csv", 4194304, "salty");
        generator.iterator().next();
        tearDown();
        assertFalse(new File("test_data.csv").exists());
    }
}
