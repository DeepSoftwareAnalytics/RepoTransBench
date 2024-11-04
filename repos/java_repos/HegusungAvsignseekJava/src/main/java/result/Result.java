package result;

import utils.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static utils.Utils.*;

public class Result {

    public static void printResults(byte[] fileBin, List<int[]> signatureRangeList, String outputFile) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(outputFile))) {
            out.write("=== AVSignSeek ===\n");

            for (int[] signatureRange : signatureRangeList) {
                int start = signatureRange[0];
                int end = signatureRange[1];

                System.out.printf("[+] Signature between bytes %d and %d%n", start, end);
                out.write(String.format("[+] Signature between bytes %d and %d%n", start, end));

                System.out.println("[+] Bytes:");
                out.write("[+] Bytes:\n");

                byte[] b = new byte[end - start];
                System.arraycopy(fileBin, start, b, 0, b.length);

                while (b.length > 0) {
                    byte[] row = new byte[Math.min(16, b.length)];
                    System.arraycopy(b, 0, row, 0, row.length);

                    StringBuilder outputLine = new StringBuilder();
                    for (byte value : row)
                        outputLine.append(String.format("%02x ", value));

                    outputLine.append(" ".repeat(Math.max(0, 60 - outputLine.length())));
                    for (byte value : row)
                        outputLine.append(Character.isISOControl((char) value) ? '.' : (char) value);

                    System.out.println(outputLine.toString());
                    out.write(outputLine.toString() + "\n");

                    byte[] newB = new byte[b.length - row.length];
                    System.arraycopy(b, row.length, newB, 0, newB.length);
                    b = newB;
                }

                b = new byte[end - start];
                System.arraycopy(fileBin, start, b, 0, b.length);

                System.out.println("[+] Strings:");
                out.write("[+] Strings:\n");
                for (String s : strings(b, 4)) {
                    System.out.printf("> %s%n", s);
                    out.write("> " + s + "\n");
                }
            }
        }
    }
}
