import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import reeds_shepp.PathElement;
import reeds_shepp.Steering;
import reeds_shepp.Gear;
import reeds_shepp.ReedsSheppUtils;

public class TestReedsShepp {

    public static class TestPathElement {

        private PathElement element;

        @BeforeEach
        public void setUp() {
            element = PathElement.create(13, Steering.LEFT, Gear.FORWARD);
        }

        @Test
        public void test_repr() {
            assertEquals(
                "{ Steering: LEFT\tGear: FORWARD\tdistance: 13 }",
                element.toString()
            );
        }

        @Test
        public void test_reverse_gear() {
            assertEquals(
                Gear.BACKWARD,
                element.reverseGear().getGear()
            );
        }

        @Test
        public void test_reverse_steering() {
            assertEquals(
                Steering.RIGHT,
                element.reverseSteering().getSteering()
            );
        }

        @Test
        public void test_with_negative_parameter() {
            PathElement element = PathElement.create(-1, Steering.LEFT, Gear.FORWARD);
            assertEquals(
                PathElement.create(1, Steering.LEFT, Gear.BACKWARD),
                element
            );
        }
    }

    public static class TestPathLength {

        @Test
        public void test_with_positive_path_elements() {
            PathElement[] path = {
                PathElement.create(1, Steering.LEFT, Gear.FORWARD),
                PathElement.create(1, Steering.LEFT, Gear.FORWARD)
            };
            assertEquals(2, ReedsSheppUtils.pathLength(path));
        }
    }

    public static class TestTimeflip {

        private PathElement[] path;
        private PathElement[] timeflipped;

        @BeforeEach
        public void setUp() {
            path = new PathElement[]{
                PathElement.create(1, Steering.LEFT, Gear.FORWARD),
                PathElement.create(1, Steering.LEFT, Gear.BACKWARD)
            };
            timeflipped = ReedsSheppUtils.timeflip(path);
        }

        @Test
        public void test_it_flips_forward_backward() {
            assertEquals(Gear.BACKWARD, timeflipped[0].getGear());
            assertEquals(Gear.FORWARD, timeflipped[1].getGear());
        }

        @Test
        public void test_it_does_not_mutate_original_path() {
            assertEquals(Gear.FORWARD, path[0].getGear());
        }
    }

    public static class TestReflect {

        private PathElement[] path;
        private PathElement[] reflected;

        @BeforeEach
        public void setUp() {
            path = new PathElement[]{
                PathElement.create(1, Steering.LEFT, Gear.FORWARD),
                PathElement.create(1, Steering.STRAIGHT, Gear.FORWARD),
                PathElement.create(1, Steering.RIGHT, Gear.FORWARD)
            };
            reflected = ReedsSheppUtils.reflect(path);
        }

        @Test
        public void test_it_reflects_steering() {
            assertEquals(Steering.RIGHT, reflected[0].getSteering());
            assertEquals(Steering.STRAIGHT, reflected[1].getSteering());
            assertEquals(Steering.LEFT, reflected[2].getSteering());
        }

        @Test
        public void test_it_does_not_mutate_original_path() {
            assertEquals(Steering.LEFT, path[0].getSteering());
        }
    }

    public static class TestGetOptimalPath {

        @Test
        public void test_smoke_test() {
            PathElement[] path = ReedsSheppUtils.getOptimalPath(new double[]{0, 0, 0}, new double[]{1, 0, 0});
            assertArrayEquals(
                new PathElement[]{PathElement.create(1.0, Steering.STRAIGHT, Gear.FORWARD)},
                path
            );
        }
    }
}
