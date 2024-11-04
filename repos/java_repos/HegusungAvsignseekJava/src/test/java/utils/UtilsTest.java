package utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

    @Test
    public void testStrings() {
        byte[] binary = "test\0string".getBytes();
        List<String> result = Utils.strings(binary, 4);
        assertEquals(List.of("test", "string"), result);
    }

    @Test
    public void testUnion() {
        List<int[]> rangeList = List.of(new int[]{0, 5}, new int[]{4, 10});
        List<int[]> result = Utils.union(rangeList);
        assertArrayEquals(new int[]{0, 10}, result.get(0));
    }

    @Test
    public void testIntersect() {
        List<int[]> rangeList = List.of(new int[]{0, 5}, new int[]{3, 10});
        int[] result = Utils.intersect(rangeList);
        assertArrayEquals(new int[]{3, 5}, result);
    }

    @Test
    public void testGenerateRanges() {
        List<int[]> selectedRangeList = List.of(new int[]{0, 99});
        int subdiv = 4;
        int minSectionSize = 10;
        Pair<List<int[]>, Boolean> result = Utils.generateRanges(selectedRangeList, subdiv, minSectionSize);
        assertFalse(result.getValue()); // minimal_range_set should be false
    }

    @Test
    public void testGetRangesFromStr() {
        String rangesStr = "0:10,20:30";
        int fileSize = 100;
        List<int[]> result = Utils.getRangesFromStr(rangesStr, fileSize);
        assertArrayEquals(new int[]{0, 10}, result.get(0));
        assertArrayEquals(new int[]{20, 30}, result.get(1));
    }

    @Test
    public void testGetReplacingValueFromStr() {
        String replacingValueStr = "0x00";
        int result = Utils.getReplacingValueFromStr(replacingValueStr);
        assertEquals(0, result);
    }
}
