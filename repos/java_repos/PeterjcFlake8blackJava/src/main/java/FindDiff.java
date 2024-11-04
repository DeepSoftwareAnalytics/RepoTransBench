package main.java;

public class FindDiff {

    public static int[] findDiffStart(String oldSrc, String newSrc) {
        String[] oldLines = oldSrc.split("\n");
        String[] newLines = newSrc.split("\n");

        for (int line = 0; line < Math.min(oldLines.length, newLines.length); line++) {
            String oldLine = oldLines[line];
            String newLine = newLines[line];
            if (oldLine.equals(newLine)) {
                continue;
            }
            for (int col = 0; col < Math.min(oldLine.length(), newLine.length()); col++) {
                if (oldLine.charAt(col) != newLine.charAt(col)) {
                    return new int[]{line, col};
                }
            }
            return new int[]{line, Math.min(oldLine.length(), newLine.length())};
        }
        return new int[]{Math.min(oldLines.length, newLines.length), 0};
    }
}
