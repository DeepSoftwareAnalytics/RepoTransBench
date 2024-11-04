import static org.junit.Assert.*;
import org.junit.Test;

public class TestLatLon {

    @Test
    public void testBasics() {
        LatLon llRad = LatLon.fromRadians(Math.PI / 4, Math.PI / 2);
        assertEquals(Math.PI / 4, llRad.getLat().getRadians(), 0.000001);
        assertEquals(Math.PI / 2, llRad.getLon().getRadians(), 0.000001);
        assertTrue(llRad.isValid());

        LatLon llDeg = LatLon.fromDegrees(45, 90);
        assertEquals(llRad, llDeg);
        assertFalse(LatLon.fromDegrees(-91, 0).isValid());
        assertFalse(LatLon.fromDegrees(0, 181).isValid());

        LatLon bad = LatLon.fromDegrees(120, 200);
        assertFalse(bad.isValid());
        LatLon better = bad.normalized();
        assertTrue(better.isValid());
        assertEquals(Angle.fromDegrees(90), better.getLat());
        assertEquals(Angle.fromDegrees(-160).getRadians(),
                     better.getLon().getRadians(), 0.000001);

        assertTrue(
            LatLon.fromDegrees(10, 20).add(LatLon.fromDegrees(20, 30))
            .approxEquals(LatLon.fromDegrees(30, 50), 0.000001));
        assertTrue(
            LatLon.fromDegrees(10, 20).subtract(LatLon.fromDegrees(20, 30))
            .approxEquals(LatLon.fromDegrees(-10, -10), 0.000001));

        LatLon invalid = LatLon.invalid();
        assertFalse(invalid.isValid());

        LatLon defaultLL = LatLon.defaultLatLon();
        assertTrue(defaultLL.isValid());
        assertEquals(0, defaultLL.getLat().getRadians(), 0.000001);
        assertEquals(0, defaultLL.getLon().getRadians(), 0.000001);
    }

    @Test
    public void testConversion() {
        assertEquals(90.0, 
            LatLon.fromPoint(LatLon.fromDegrees(90.0, 65.0).toPoint())
            .getLat().getDegrees(), 0.000001);

        assertEquals(-Math.PI / 2, 
            LatLon.fromPoint(LatLon.fromRadians(-Math.PI / 2, 1).toPoint())
            .getLat().getRadians(), 0.000001);

        assertEquals(180.0, 
            Math.abs(LatLon.fromPoint(LatLon.fromDegrees(12.2, 180.0).toPoint())
            .getLon().getDegrees()), 0.000001);

        assertEquals(Math.PI, 
            Math.abs(LatLon.fromPoint(LatLon.fromRadians(0.1, -Math.PI).toPoint())
            .getLon().getRadians()), 0.000001);
    }

    @Test
    public void testDistance() {
        assertEquals(0.0, 
            LatLon.fromDegrees(90, 0).getDistance(LatLon.fromDegrees(90, 0))
            .getRadians(), 0.000001);

        assertEquals(77.0, 
            LatLon.fromDegrees(-37, 25).getDistance(LatLon.fromDegrees(-66, -155))
            .getDegrees(), 1e-13);

        assertEquals(115.0, 
            LatLon.fromDegrees(0, 165).getDistance(LatLon.fromDegrees(0, -80))
            .getDegrees(), 1e-13);

        assertEquals(180.0, 
            LatLon.fromDegrees(47, -127).getDistance(LatLon.fromDegrees(-47, 53))
            .getDegrees(), 2e-6);
    }
}
