package avsignseek;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import raw_analysis.RawAnalysis;
import result.Result;
import utils.Utils;

public class AVSignSeekTest {

    private AVSignSeek avSignSeek;
    
    @BeforeEach
    public void setUp() {
        avSignSeek = new AVSignSeek();
    }
    
    @Test
    public void testMainFunction() throws IOException, InterruptedException {
        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class);
             MockedStatic<Result> resultMock = mockStatic(Result.class)) {

            byte[] mockFileBin = new byte[100];
            List<int[]> mockRanges = List.of(new int[]{0, 100});
            List<int[]> mockSignatureRanges = List.of(new int[]{0, 50});
            
            RawAnalysis analysis = mock(RawAnalysis.class);

            utilsMock.when(() -> Utils.getRangesFromStr(anyString(), anyInt())).thenReturn(mockRanges);
            utilsMock.when(() -> Utils.getReplacingValueFromStr(anyString())).thenReturn(0);
            when(analysis.rawAnalysis(eq(mockFileBin), eq(mockRanges), anyInt(), anyString(), anyInt(), anyBoolean(), anyInt(), anyInt())).thenReturn(mockSignatureRanges);

            AVSignSeek.main(new String[]{});

            utilsMock.verify(() -> Utils.getRangesFromStr(anyString(), anyInt()), times(1));
            utilsMock.verify(() -> Utils.getReplacingValueFromStr(anyString()), times(1));
            verify(analysis, times(1)).rawAnalysis(eq(mockFileBin), eq(mockRanges), anyInt(), anyString(), anyInt(), anyBoolean(), anyInt(), anyInt());
            resultMock.verify(() -> Result.printResults(eq(mockFileBin), eq(mockSignatureRanges), anyString()), times(1));
        }
    }
}
