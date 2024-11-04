import org.junit.Test;
import static org.junit.Assert.*;

public class TestCellId2 {

    @Test
    public void testCellId2() {
        double lat = 33;
        double lon = -122;

        // 使用 Java 版本的 S2 库获取 CellId
        CellId pyCellId = CellId.fromLatLon(LatLon.fromDegrees(lat, lon));

        // 使用 C++ 绑定库的 Java 接口获取 CellId
        S2CellId cppCellId = S2CellId.fromLatLng(S2LatLng.fromDegrees(lat, lon));

        // 断言两个 CellId 的 ID 值是否相等
        assertEquals(pyCellId.id(), cppCellId.id());
    }

    public void cellIdParentComparison(int level) {
        double lat = 33;
        double lon = -122;

        // 获取不同级别的父 CellId 对象
        CellId pyCellId = CellId.fromLatLon(LatLon.fromDegrees(lat, lon)).parent(level);
        S2CellId cppCellId = S2CellId.fromLatLng(S2LatLng.fromDegrees(lat, lon)).parent(level);

        // 断言父级 CellId 的 ID 值是否相等
        assertEquals(pyCellId.id(), cppCellId.id());
    }

    @Test
    public void testCellIdParents() {
        // 对不同的层级进行测试
        for (int level = 1; level <= 30; level++) {
            cellIdParentComparison(level);
        }
    }
}
