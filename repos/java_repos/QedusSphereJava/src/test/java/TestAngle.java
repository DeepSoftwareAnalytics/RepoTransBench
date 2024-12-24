import static org.junit.Assert.*;
import org.junit.Test;

public class TestAngle {

    @Test
    public void testDefaultConstructor() {
        Angle angle = new Angle();
        assertEquals(0, angle.getRadians(), 0.000001); // 使用 getRadians 获取值
    }

    @Test
    public void testPiRadiansExactly180Degrees() {
        // 测试 fromRadians 和 fromDegrees 方法
        assertEquals(Math.PI, Angle.fromRadians(Math.PI).getRadians(), 0.000001);
        assertEquals(180.0, Angle.fromRadians(Math.PI).getDegrees(), 0.000001);
        assertEquals(Math.PI, Angle.fromDegrees(180).getRadians(), 0.000001);
        assertEquals(180.0, Angle.fromDegrees(180).getDegrees(), 0.000001);

        // 负角度测试
        assertEquals(-90.0, Angle.fromRadians(-Math.PI / 2).getDegrees(), 0.000001);
        assertEquals(-Math.PI / 4, Angle.fromDegrees(-45).getRadians(), 0.000001);
    }
}
