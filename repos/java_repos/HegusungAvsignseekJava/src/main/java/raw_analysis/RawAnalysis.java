package raw_analysis;

import utils.Pair;
import utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static utils.Utils.*;

public class RawAnalysis {

    public List<int[]> rawAnalysis(byte[] fileBin, List<int[]> analysedParts,
                                   int signMinSize, String testDir, int subdiv,
                                   boolean manual, int sleep, int replacingValue) throws IOException, InterruptedException {

        Pair<List<int[]>, Boolean> result = Utils.generateRanges(analysedParts, subdiv, signMinSize);
        List<int[]> rangeList = result.getKey();
        boolean minimalRangeSet = result.getValue();

        if (minimalRangeSet) {
            return rangeList;
        }

        List<int[]> newRangeList = new ArrayList<>();
        List<File> tempFiles = new ArrayList<>();

        for (int i = 0; i < rangeList.size(); i++) {
            int[] r = rangeList.get(i);
            File tempFile = new File(testDir, "test-" + i + ".bin");
            tempFiles.add(tempFile);

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileBin, 0, r[0]);
                fos.write(new byte[r[1] - r[0] + 1]);
                fos.write(fileBin, r[1] + 1, fileBin.length - r[1] - 1);
            }
        }

        if (!manual) {
            TimeUnit.SECONDS.sleep(sleep);
        } else {
            System.in.read();
        }

        for (File tempFile : tempFiles) {
            if (tempFile.exists()) {
                int[] range = rangeList.get(tempFiles.indexOf(tempFile));
                newRangeList.add(range);
                System.out.printf("[i] Located signature between bytes %d and %d%n", range[0], range[1]);
            }
            Files.delete(tempFile.toPath());
        }

        if (newRangeList.isEmpty() || union(newRangeList).equals(union(analysedParts))) {
            return newRangeList;
        } else {
            return rawAnalysis(fileBin, newRangeList, signMinSize, testDir, subdiv, manual, sleep, replacingValue);
        }
    }
}

