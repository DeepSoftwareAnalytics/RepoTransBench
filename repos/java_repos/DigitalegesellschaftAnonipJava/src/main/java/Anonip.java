import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import org.apache.commons.net.util.SubnetUtils;

public class Anonip {
    private static final Logger logger = Logger.getLogger(Anonip.class.getName());

    private List<Integer> columns;
    private final int[] prefixes = new int[2];
    private int ipv4mask;
    private int ipv6mask;
    private long increment;
    private String delimiter;
    private String replace;
    private Pattern regex;
    private boolean skipPrivate;

    public Anonip(
        List<Integer> columns, int ipv4mask, int ipv6mask, long increment, String delimiter,
        String replace, Pattern regex, boolean skipPrivate) {
        this.columns = columns != null ? adjustColumns(columns) : List.of(0);
        setIpv4mask(ipv4mask);
        setIpv6mask(ipv6mask);
        this.increment = increment;
        this.delimiter = delimiter;
        this.replace = replace;
        this.regex = regex;
        this.skipPrivate = skipPrivate;
    }

    private List<Integer> adjustColumns(List<Integer> columns) {
        List<Integer> adjustedColumns = new ArrayList<>();
        for (int col : columns) {
            adjustedColumns.add(col - 1);
        }
        return adjustedColumns;
    }

    public int getIpv4mask() {
        return ipv4mask;
    }

    public void setIpv4mask(int ipv4mask) {
        this.ipv4mask = ipv4mask;
        this.prefixes[0] = 32 - ipv4mask;
    }

    public int getIpv6mask() {
        return ipv6mask;
    }

    public void setIpv6mask(int ipv6mask) {
        this.ipv6mask = ipv6mask;
        this.prefixes[1] = 128 - ipv6mask;
    }

    public void process(InputStream inputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                logger.log(Level.FINE, "Empty line detected. Doing nothing.");
                continue;
            }
            logger.log(Level.FINE, "Got line: " + line);
            System.out.println(processLine(line));
        }
    }

    public String processLine(String line) {
        if (replace != null) {
            return line.replaceAll("\\b[^\\s]+\\b", replace);
        }
        if (regex != null) {
            return processLineWithRegex(line);
        }
        return processLineWithColumns(line);
    }

    private String processLineWithRegex(String line) {
        Matcher matcher = regex.matcher(line);
        if (!matcher.find()) {
            logger.log(Level.FINE, "Regex did not match!");
            return line;
        }
        StringBuffer result = new StringBuffer();
        matcher.appendReplacement(result, processIp(matcher.group()));
        matcher.appendTail(result);
        return result.toString();
    }

    private String processLineWithColumns(String line) {
        String[] columns = line.split(delimiter);
        for (int index : this.columns) {
            if (index >= columns.length) {
                logger.log(Level.WARNING, "Column " + (index + 1) + " does not exist!");
                continue;
            }
            String column = columns[index];
            if (column.isEmpty()) {
                logger.log(Level.FINE, "Column " + (index + 1) + " is empty.");
                continue;
            }
            columns[index] = processIp(column);
        }
        return String.join(delimiter, columns);
    }

    private String processIp(String ipString) {
        URI uri;
        try {
            uri = new URI("http://" + ipString);
        } catch (URISyntaxException e) {
            return ipString;
        }
        String host = uri.getHost();
        if (host == null) {
            return ipString;
        }
        String port = uri.getPort() == -1 ? "" : ":" + uri.getPort();
        if (skipPrivate && isPrivateIp(host)) {
            return ipString;
        }
        SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils(host + "/" + prefixes[host.contains(":") ? 1 : 0]).getInfo();
        String truncatedIp = subnetInfo.getNetworkAddress();
        if (increment != 0) {
            String[] parts = truncatedIp.split("\\.");
            long ipNum = 0;
            for (String part : parts) {
                ipNum = ipNum * 256 + Long.parseLong(part);
            }
            ipNum = (ipNum + increment) & 0xFFFFFFFFL;
            truncatedIp = String.format("%d.%d.%d.%d",
                    (ipNum >> 24) & 0xFF, (ipNum >> 16) & 0xFF, (ipNum >> 8) & 0xFF, ipNum & 0xFF);
        }
        return truncatedIp + port;
    }

    private boolean isPrivateIp(String ip) {
        String regex = "^(10|127|192\\.168|172\\.(1[6-9]|2[0-9]|3[01]))\\..*";
        return ip.matches(regex);
    }
}
