package avsignseek;

import raw_analysis.RawAnalysis;
import result.Result;
import utils.Utils;

import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static utils.Utils.*;

public class AVSignSeek {

    public static void main(String[] args) {
        try {
            // Sample arguments, would be replaced with actual arguments in a real app
            String zipFile = "test.zip";
            String zipPassword = "infected";
            String filename = "infected.bin";
            String rangesStr = ":";
            String replacingValue = "0x00";
            int limitSign = 64;
            String testDir = ".";
            int subdiv = 4;
            boolean manual = false;
            int sleep = 20;
            String outputFile = "output.txt";

            byte[] fileBin = readFileFromZip(zipFile, filename, zipPassword);
            List<int[]> analysedParts = getRangesFromStr(rangesStr, fileBin.length);
            int replacingValueInt = getReplacingValueFromStr(replacingValue);

            RawAnalysis analysis = new RawAnalysis();
            List<int[]> signatureRangeList = analysis.rawAnalysis(fileBin, analysedParts, limitSign, testDir, subdiv, manual, sleep, replacingValueInt);

            signatureRangeList = union(signatureRangeList);
            Result.printResults(fileBin, signatureRangeList, outputFile);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static byte[] readFileFromZip(String zipFilePath, String filename, String password) throws IOException {
        FileInputStream fis = new FileInputStream(zipFilePath);

        try (ZipInputStream zis = new ZipInputStream(fis, StandardCharsets.UTF_8)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.getName().equals(filename)) {
                    return IOUtils.toByteArray(zis);
                }
            }
        }
        throw new IOException("Failed to find file in the zip: " + filename);
    }
}
