import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppleHealthDataExtractor {

    private static final Map<String, String> RECORD_FIELDS;
    private static final Map<String, String> ACTIVITY_SUMMARY_FIELDS;
    private static final Map<String, String> WORKOUT_FIELDS;
    private static final Map<String, Map<String, String>> FIELDS;

    static {
        RECORD_FIELDS = new LinkedHashMap<>();
        RECORD_FIELDS.put("sourceName", "s");
        RECORD_FIELDS.put("sourceVersion", "s");
        RECORD_FIELDS.put("device", "s");
        RECORD_FIELDS.put("type", "s");
        RECORD_FIELDS.put("unit", "s");
        RECORD_FIELDS.put("creationDate", "d");
        RECORD_FIELDS.put("startDate", "d");
        RECORD_FIELDS.put("endDate", "d");
        RECORD_FIELDS.put("value", "n");

        ACTIVITY_SUMMARY_FIELDS = new LinkedHashMap<>();
        ACTIVITY_SUMMARY_FIELDS.put("dateComponents", "d");
        ACTIVITY_SUMMARY_FIELDS.put("activeEnergyBurned", "n");
        ACTIVITY_SUMMARY_FIELDS.put("activeEnergyBurnedGoal", "n");
        ACTIVITY_SUMMARY_FIELDS.put("activeEnergyBurnedUnit", "s");
        ACTIVITY_SUMMARY_FIELDS.put("appleExerciseTime", "s");
        ACTIVITY_SUMMARY_FIELDS.put("appleExerciseTimeGoal", "s");
        ACTIVITY_SUMMARY_FIELDS.put("appleStandHours", "n");
        ACTIVITY_SUMMARY_FIELDS.put("appleStandHoursGoal", "n");

        WORKOUT_FIELDS = new LinkedHashMap<>();
        WORKOUT_FIELDS.put("sourceName", "s");
        WORKOUT_FIELDS.put("sourceVersion", "s");
        WORKOUT_FIELDS.put("device", "s");
        WORKOUT_FIELDS.put("creationDate", "d");
        WORKOUT_FIELDS.put("startDate", "d");
        WORKOUT_FIELDS.put("endDate", "d");
        WORKOUT_FIELDS.put("workoutActivityType", "s");
        WORKOUT_FIELDS.put("duration", "n");
        WORKOUT_FIELDS.put("durationUnit", "s");
        WORKOUT_FIELDS.put("totalDistance", "n");
        WORKOUT_FIELDS.put("totalDistanceUnit", "s");
        WORKOUT_FIELDS.put("totalEnergyBurned", "n");
        WORKOUT_FIELDS.put("totalEnergyBurnedUnit", "s");

        FIELDS = new HashMap<>();
        FIELDS.put("Record", RECORD_FIELDS);
        FIELDS.put("ActivitySummary", ACTIVITY_SUMMARY_FIELDS);
        FIELDS.put("Workout", WORKOUT_FIELDS);
    }

    private static final Pattern PREFIX_RE = Pattern.compile("^HK.*TypeIdentifier(.+)$");
    private static final boolean ABBREVIATE = true;
    private static final boolean VERBOSE = true;

    private final String inPath;
    private boolean verbose;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private Element root;

    private HashMap<String, Integer> tags = new HashMap<>();
    private HashMap<String, Integer> fields = new HashMap<>();
    private HashMap<String, Integer> recordTypes = new HashMap<>();
    private HashMap<String, Integer> otherTypes = new HashMap<>();
    private List<Element> nodes = new ArrayList<>();
    private List<Map<String, String>> items = new ArrayList<>();
    private Map<String, File> handles = new HashMap<>();
    private String directory;

    public AppleHealthDataExtractor(String path, boolean verbose) throws ParserConfigurationException, IOException, SAXException {
        this.inPath = path;
        this.verbose = verbose;
        this.factory = DocumentBuilderFactory.newInstance();
        this.builder = factory.newDocumentBuilder();

        File xmlFile = new File(path);
        this.directory = xmlFile.getParent();
        this.root = builder.parse(xmlFile).getDocumentElement();
        this.nodes = new ArrayList<>();
        this.abbreviateTypes();
        this.collectStats();
    }

    private void report(String msg) {
        if (this.verbose) {
            System.out.println(msg);
        }
    }

    // Functions to count tags, fields, and types

    private void abbreviateTypes() {
        NodeList nodeList = root.getElementsByTagName("*");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element node = (Element) nodeList.item(i);
            if (node.getNodeName().equals("Record") && node.hasAttribute("type")) {
                String type = node.getAttribute("type");
                Matcher m = PREFIX_RE.matcher(type);
                if (m.matches()) {
                    node.setAttribute("type", m.group(1));
                }
            }
        }
    }

    private void collectStats() {
        NodeList nodeList = root.getElementsByTagName("*");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element node = (Element) nodeList.item(i);
            String nodeName = node.getNodeName();
            tags.put(nodeName, tags.getOrDefault(nodeName, 0) + 1);

            if (FIELDS.containsKey(nodeName)) {
                Map<String, String> fieldsMap = FIELDS.get(nodeName);

                for (String fieldName : fieldsMap.keySet()) {
                    if (node.hasAttribute(fieldName)) {
                        fields.put(fieldName, fields.getOrDefault(fieldName, 0) + 1);
                    }
                }

                if (nodeName.equals("Record")) {
                    String type = node.getAttribute("type");
                    recordTypes.put(type, recordTypes.getOrDefault(type, 0) + 1);
                } else {
                    otherTypes.put(nodeName, otherTypes.getOrDefault(nodeName, 0) + 1);
                }
            }
        }
    }

    // Utility Functions

    private static String formatValue(String value, String datatype) {
        if (value == null) {
            return "";
        } else if (datatype.equals("s")) {
            return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        } else if (datatype.equals("n") || datatype.equals("d")) {
            return value;
        } else {
            throw new IllegalArgumentException("Unexpected format value: " + datatype);
        }
    }

    public void extract() {
        // Implementation to read XML, extract data and write to CSV
    }
}

