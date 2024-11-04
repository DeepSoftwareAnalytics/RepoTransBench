import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class CoveringTest {

    @Test
    public void testCovering() {
        RegionCoverer coverer = new RegionCoverer();
        LatLon p1 = LatLon.fromDegrees(33, -122);
        LatLon p2 = LatLon.fromDegrees(33.1, -122.1);
        
        // 获取覆盖区域
        LatLonRect rect = LatLonRect.fromPointPair(p1, p2);
        List<CellId> cellIds = coverer.getCovering(rect);

        // 将 CellId 转换为 ID 并进行排序
        List<Long> ids = new ArrayList<>();
        for (CellId cellId : cellIds) {
            ids.add(cellId.id());
        }
        Collections.sort(ids);

        // 预期目标 ID 列表
        List<Long> target = Arrays.asList(
            9291041754864156672L,
            9291043953887412224L,
            9291044503643226112L,
            9291045878032760832L,
            9291047252422295552L,
            9291047802178109440L,
            9291051650468806656L,
            9291052200224620544L
        );

        // 断言结果是否相同
        assertEquals(ids, target);
    }
}
