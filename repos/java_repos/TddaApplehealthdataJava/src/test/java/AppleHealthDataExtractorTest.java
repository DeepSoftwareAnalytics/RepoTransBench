import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AppleHealthDataExtractorTest {

    private static final boolean CLEAN_UP = true;
    private static final boolean VERBOSE = false;

    private Path testdataDir;
    private Path tmpDir;

    @BeforeEach
    void setUp() throws Exception {
        this.testdataDir = Paths.get("testdata");
        this.tmpDir = Files.createTempDirectory("tmp");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (CLEAN_UP) {
            Files.walk(tmpDir)
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    private void checkFile(String filename) throws IOException {
        Path expectedOutput = testdataDir.resolve(filename);
        Path actualOutput = tmpDir.resolve(filename);
        String expected = new String(Files.readAllBytes(expectedOutput), StandardCharsets.UTF_8);
        String actual = new String(Files.readAllBytes(actualOutput), StandardCharsets.UTF_8);
        assertEquals(expected, actual);
    }

    @Test
    void testTinyReferenceExtraction() throws Exception {
        Path xmlFilePath = Files.copy(testdataDir.resolve("export6s3sample.xml"), tmpDir.resolve("export6s3sample.xml"));
        AppleHealthDataExtractor data = new AppleHealthDataExtractor(xmlFilePath.toString(), VERBOSE);
        data.extract();

        for (String kind : new String[]{"StepCount", "DistanceWalkingRunning", "Workout", "ActivitySummary"}) {
            checkFile(kind + ".csv");
        }
    }

    @Test
    void testFormatFreqs() {
        Map<String, Integer> counts = new HashMap<>();
        assertEquals(AppleHealthDataExtractor.formatFreqs(counts), "");

        counts.put("one", 1);
        assertEquals(AppleHealthDataExtractor.formatFreqs(counts), "one: 1");

        counts.put("one", 2);
        assertEquals(AppleHealthDataExtractor.formatFreqs(counts), "one: 2");

        counts.put("two", 1);
        counts.put("three", 1);
        String expected = "one: 2\nthree: 1\ntwo: 1";
        assertEquals(AppleHealthDataExtractor.formatFreqs(counts), expected);
    }

    @Test
    void testFormatNullValues() {
        for (String dt : new String[]{"s", "n", "d", "z"}) {
            assertEquals(AppleHealthDataExtractor.formatValue(null, dt), "");
        }
    }

    @Test
    void testFormatNumericValues() {
        Map<String, String> cases = Stream.of(new String[][] {
            { "0", "0" },
            { "3", "3" },
            { "-1", "-1" },
            { "2.5", "2.5" }
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        for (Map.Entry<String, String> entry : cases.entrySet()) {
            assertEquals(AppleHealthDataExtractor.formatValue(entry.getKey(), "n"), entry.getValue());
        }
    }

    @Test
    void testFormatDateValues() {
        String hearts = "any string not need escaping or quoting; even this: ♥♥";
        Map<String, String> cases = Stream.of(new String[][] {
            { "01/02/2000 12:34:56", "01/02/2000 12:34:56" },
            { hearts, hearts }
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        for (Map.Entry<String, String> entry : cases.entrySet()) {
            assertEquals(AppleHealthDataExtractor.formatValue(entry.getKey(), "d"), entry.getValue());
        }
    }

    @Test
    void testFormatStringValues() {
        Map<String, String> cases = Stream.of(new String[][] {
            { "a", "\"a\"" },
            { "", "\"\"" },
            { "one \"2\" three", "\"one \\\"2\\\" three\"" },
            { "1\\2\\3", "\"1\\\\2\\\\3\"" }
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        for (Map.Entry<String, String> entry : cases.entrySet()) {
            assertEquals(AppleHealthDataExtractor.formatValue(entry.getKey(), "s"), entry.getValue());
        }
    }

    @Test
    void testAbbreviate() {
        Map<String, String> changed = Stream.of(new String[][] {
            { "HKQuantityTypeIdentifierHeight", "Height" },
            { "HKQuantityTypeIdentifierStepCount", "StepCount" },
            { "HK*TypeIdentifierStepCount", "StepCount" },
            { "HKCharacteristicTypeIdentifierDateOfBirth", "DateOfBirth" },
            { "HKCharacteristicTypeIdentifierBiologicalSex", "BiologicalSex" },
            { "HKCharacteristicTypeIdentifierBloodType", "BloodType" },
            { "HKCharacteristicTypeIdentifierFitzpatrickSkinType", "FitzpatrickSkinType" }
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        for (Map.Entry<String, String> entry : changed.entrySet()) {
            assertEquals(AppleHealthDataExtractor.abbreviate(entry.getKey()), entry.getValue());
            assertEquals(AppleHealthDataExtractor.abbreviate(entry.getKey(), false), entry.getKey());
        }

        String[] unchanged = {
            "", "a", "aHKQuantityTypeIdentifierHeight", "HKQuantityTypeIdentityHeight"
        };

        for (String key : unchanged) {
            assertEquals(AppleHealthDataExtractor.abbreviate(key), key);
        }
    }

    @Test
    void testEncode() {
        assertTrue(AppleHealthDataExtractor.encode("a") instanceof String);
    }

    @Test
    void testExtractedReferenceStats() throws Exception {
        Path xmlFilePath = Files.copy(testdataDir.resolve("export6s3sample.xml"), tmpDir.resolve("export6s3sample.xml"));
        AppleHealthDataExtractor data = new AppleHealthDataExtractor(xmlFilePath.toString(), VERBOSE);

        assertEquals(20, data.getNNodes());

        Map<String, Integer> expectedRecordCounts = Stream.of(new String[][] {
            { "DistanceWalkingRunning", "5" },
            { "StepCount", "10" }
        }).collect(Collectors.toMap(dataArray -> dataArray[0], dataArray -> Integer.parseInt(dataArray[1])));
        assertEquals(expectedRecordCounts, data.getRecordTypes());

        assertEquals(20, data.getNNodes());

        Map<String, Integer> expectedOtherCounts = Stream.of(new String[][] {
            { "ActivitySummary", "2" },
            { "Workout", "1" }
        }).collect(Collectors.toMap(dataArray -> dataArray[0], dataArray -> Integer.parseInt(dataArray[1])));
        assertEquals(expectedOtherCounts, data.getOtherTypes());

        Map<String, Integer> expectedTagCounts = Stream.of(new String[][] {
            { "ActivitySummary", "2" },
            { "ExportDate", "1" },
            { "Me", "1" },
            { "Record", "15" },
            { "Workout", "1" }
        }).collect(Collectors.toMap(dataArray -> dataArray[0], dataArray -> Integer.parseInt(dataArray[1])));
        assertEquals(expectedTagCounts, data.getTags());

        Map<String, Integer> expectedFieldCounts = Stream.of(new String[][] {
            { "HKCharacteristicTypeIdentifierBiologicalSex", "1" },
            { "HKCharacteristicTypeIdentifierBloodType", "1" },
            { "HKCharacteristicTypeIdentifierDateOfBirth", "1" },
            { "HKCharacteristicTypeIdentifierFitzpatrickSkinType", "1" },
            { "activeEnergyBurned", "2" },
            { "activeEnergyBurnedGoal", "2" },
            { "activeEnergyBurnedUnit", "2" },
            { "appleExerciseTime", "2" },
            { "appleExerciseTimeGoal", "2" },
            { "appleStandHours", "2" },
            { "appleStandHoursGoal", "2" },
            { "creationDate", "16" },
            { "dateComponents", "2" },
            { "duration", "1" },
            { "durationUnit", "1" },
            { "endDate", "16" },
            { "sourceName", "16" },
            { "sourceVersion", "1" },
            { "startDate", "16" },
            { "totalDistance", "1" },
            { "totalDistanceUnit", "1" },
            { "totalEnergyBurned", "1" },
            { "totalEnergyBurnedUnit", "1" },
            { "type", "15" },
            { "unit", "15" },
            { "value", "16" },
            { "workoutActivityType", "1" }
        }).collect(Collectors.toMap(dataArray -> dataArray[0], dataArray -> Integer.parseInt(dataArray[1])));
        assertEquals(expectedFieldCounts, data.getFields());
    }
}
