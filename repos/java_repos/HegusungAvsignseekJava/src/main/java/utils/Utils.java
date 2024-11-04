package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    
    public static List<String> strings(byte[] binary, int min) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (byte b : binary) {
            char c = (char) b;
            if (Character.isISOControl(c)) {
                if (sb.length() >= min) {
                    result.add(sb.toString());
                }
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        if (sb.length() >= min) {
            result.add(sb.toString());
        }
        return result;
    }

    public static List<int[]> union(List<int[]> rangeList) {
        List<int[]> res = new ArrayList<>();
        for (int[] range : rangeList.stream().sorted((a, b) -> a[0] - b[0]).collect(Collectors.toList())) {
            if (!res.isEmpty() && res.get(res.size() - 1)[1] >= range[0] - 1) {
                res.get(res.size() - 1)[1] = Math.max(res.get(res.size() - 1)[1], range[1]);
            } else {
                res.add(new int[]{range[0], range[1]});
            }
        }
        return res;
    }

    public static int[] intersect(List<int[]> rangeList) {
        int[] res = null;
        for (int[] range : rangeList.stream().sorted((a, b) -> a[0] - b[0]).collect(Collectors.toList())) {
            if (res == null) {
                res = new int[]{range[0], range[1]};
            } else if (res[1] >= range[0]) {
                res[0] = range[0];
            } else {
                return null;
            }
        }
        return res != null ? new int[]{res[0], res[1]} : null;
    }

    public static int rangeSize(int[] r) {
        return r[1] - r[0] + 1;
    }

    public static Pair<List<int[]>, Boolean> generateRanges(List<int[]> selectedRangeList, int subdiv, int minSectionSize) {
        boolean minimalRangeSet = true;

        selectedRangeList = union(selectedRangeList);
        List<int[]> res = new ArrayList<>();
        for (int[] selectedRange : selectedRangeList) {
            if (rangeSize(selectedRange) <= minSectionSize) {
                res.add(selectedRange);
                continue;
            }

            minimalRangeSet = false;
            int sectionSize = Math.max(rangeSize(selectedRange) / subdiv, minSectionSize);

            while (rangeSize(selectedRange) > 0) {
                int[] currentRange = new int[]{selectedRange[0], selectedRange[0] + sectionSize - 1};
                currentRange = intersect(Arrays.asList(selectedRange, currentRange));
                res.add(currentRange);
                selectedRange = new int[]{selectedRange[0] + sectionSize, selectedRange[1]};
            }
        }
        return new Pair<>(res, minimalRangeSet);
    }

    public static List<int[]> getRangesFromStr(String rangesStr, int fileSize) {
        List<int[]> res = new ArrayList<>();
        for (String rangeStr : rangesStr.split(",")) {
            if (!rangeStr.contains(":")) continue;

            try {
                int start = Integer.decode(rangeStr.split(":")[0].isEmpty() ? "0" : rangeStr.split(":")[0]);
                int end = Integer.decode(rangeStr.split(":")[1].isEmpty() ? String.valueOf(fileSize - 1) : rangeStr.split(":")[1]);

                if (start < 0 || end < 0 || start > end) throw new RuntimeException("Incorrect input range (example: ':0x100,0x150:0x1a0,0x1b0:')");
                res.add(new int[]{start, end});
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("Incorrect input range (example: ':0x100,0x150:0x1a0,0x1b0:')");
            }
        }
        return union(res);
    }

    public static int getReplacingValueFromStr(String replacingValueStr) {
        int replacingValue;
        if (replacingValueStr.startsWith("0x")) {
            replacingValue = Integer.decode(replacingValueStr);
        } else if (replacingValueStr.length() != 1) {
            throw new RuntimeException("Wrong replacing value, you can specify a byte by starting it with '0x' or a single Ascii character");
        } else {
            replacingValue = replacingValueStr.charAt(0);
        }

        if (replacingValue < 0 || replacingValue > 255) {
            throw new RuntimeException("Wrong replacing value, you can specify a byte by starting it with '0x' or a single Ascii character");
        }
        return replacingValue;
    }
}

