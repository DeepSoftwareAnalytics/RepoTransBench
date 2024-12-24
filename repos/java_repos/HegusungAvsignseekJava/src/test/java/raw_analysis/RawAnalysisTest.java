package raw_analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import utils.Pair;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RawAnalysisTest {

    private RawAnalysis rawAnalysis;
    
    @BeforeEach
    public void setUp() {
        rawAnalysis = new RawAnalysis();
    }
    
    @Test
    public void testRawAnalysis() throws IOException, InterruptedException {
        byte[] fileBin = new byte[100];
        List<int[]> analysedParts = List.of(new int[]{0, 99});
        int signMinSize = 10;
        String testDir = ".";
        int subdiv = 4;
        boolean manual = false;
        int sleep = 1;
        int replacingValue = 0;
        
        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class)) {
            List<int[]> expected = List.of(new int[]{0, 24}, new int[]{25, 49}, new int[]{50, 74}, new int[]{75, 99});
            utilsMock.when(() -> Utils.generateRanges(eq(analysedParts), eq(subdiv), eq(signMinSize))).thenReturn(new Pair<>(expected, false));
            utilsMock.when(() -> Utils.union(expected)).thenReturn(expected);

            List<int[]> result = rawAnalysis.rawAnalysis(fileBin, analysedParts, signMinSize, testDir, subdiv, manual, sleep, replacingValue);

            assertEquals(expected, result);
        }
    }
}
