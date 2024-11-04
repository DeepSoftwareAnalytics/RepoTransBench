import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.NoSuchMethodException;;
public class PartitionTest {

    private Partition partition;

    @BeforeEach
    public void setUp() {
        partition = new Partition();
    }

    @Test
    public void testAction() {
        assertEquals("netem loss 100%", partition.action(), "Action method failed for Partition");
    }

    @Test
    public void testDesc() {
        assertEquals("network partition", partition.desc(), "Desc method failed for Partition");
    }

    @Test
    public void testPartitionBehavior() throws NoSuchMethodException {
        assertTrue(partition instanceof Partition, "Object is not an instance of Partition");
        assertTrue(partition.getClass().getMethod("action") != null, "Partition does not have action method");
        assertTrue(partition.getClass().getMethod("desc") != null, "Partition does not have desc method");
    }
}
