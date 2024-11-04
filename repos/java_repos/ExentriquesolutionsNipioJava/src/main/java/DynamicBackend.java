import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Objects;
import org.apache.commons.net.util.SubnetUtils;

public class DynamicBackend {
    private String id;
    private String soa;
    private String domain;
    private String ipAddress;
    private String ttl;
    private Map<String, String> nameServers;
    private List<String> whitelistedRanges;
    private List<String> blacklistedIps;
    private String bits;
    private String auth;

    public DynamicBackend() {
        this.nameServers = new LinkedHashMap<>();
        this.whitelistedRanges = new ArrayList<>();
        this.blacklistedIps = new ArrayList<>();
        this.bits = "0";
        this.auth = "1";
    }

    public void configure(String configFilename) throws IOException {
        File configFile = new File(configFilename);
        if (!configFile.exists()) {
            log("file " + configFilename + " does not exist");
            System.exit(1);
        }

        Properties config = new Properties();
        try (FileReader reader = new FileReader(configFile)) {
            config.load(reader);
        }

        this.id = resolveConfig("NIPIO_SOA_ID", config, "soa.id");
        this.soa = String.join(" ",
                resolveConfig("NIPIO_SOA_NS", config, "soa.ns"),
                resolveConfig("NIPIO_SOA_HOSTMASTER", config, "soa.hostmaster"),
                this.id,
                resolveConfig("NIPIO_SOA_REFRESH", config, "soa.refresh"),
                resolveConfig("NIPIO_SOA_RETRY", config, "soa.retry"),
                resolveConfig("NIPIO_SOA_EXPIRY", config, "soa.expiry"),
                resolveConfig("NIPIO_SOA_MINIMUM_TTL", config, "soa.minimum")
        );
        this.domain = resolveConfig("NIPIO_DOMAIN", config, "main.domain");
        this.ipAddress = resolveConfig("NIPIO_NONWILD_DEFAULT_IP", config, "main.ipaddress");
        this.ttl = resolveConfig("NIPIO_TTL", config, "main.ttl");
        this.nameServers = parseEnvSplitted("NIPIO_NAMESERVERS", config, "nameservers");
        this.bits = resolveConfig("NIPIO_BITS", config, "main.bits");
        this.auth = resolveConfig("NIPIO_AUTH", config, "main.auth");

        if (System.getenv("NIPIO_WHITELIST") != null || config.containsKey("whitelist")) {
            this.whitelistedRanges = parseEnvSplitted("NIPIO_WHITELIST", config, "whitelist").values()
                    .stream().collect(Collectors.toList());
        }

        if (System.getenv("NIPIO_BLACKLIST") != null || config.containsKey("blacklist")) {
            this.blacklistedIps = parseEnvSplitted("NIPIO_BLACKLIST", config, "blacklist").values()
                    .stream().collect(Collectors.toList());
        }

        log("Name servers: " + this.nameServers);
        log("ID: " + this.id);
        log("TTL: " + this.ttl);
        log("SOA: " + this.soa);
        log("IP address: " + this.ipAddress);
        log("Domain: " + this.domain);
        log("Whitelisted IP ranges: " + this.whitelistedRanges);
        log("Blacklisted IPs: " + this.blacklistedIps);
    }

    public void run() throws IOException {
        log("starting up");
        List<String> handshake = getNext();
        if (!"5".equals(handshake.get(1))) {
            log("Not version 5: " + handshake);
            System.exit(1);
        }
        write("OK", "nip.io backend - We are good");
        log("Done handshake");

        while (true) {
            List<String> cmd = getNext();
            if ("CMD".equals(cmd.get(0))) {
                log("received command: " + cmd);
                writeEnd();
                continue;
            }

            if ("END".equals(cmd.get(0))) {
                log("completing");
                break;
            }

            if (cmd.size() < 6) {
                log("did not understand: " + cmd);
                write("FAIL");
                continue;
            }

            String qname = cmd.get(1).toLowerCase();
            String qtype = cmd.get(3);

            if (("A".equals(qtype) || "ANY".equals(qtype)) && qname.endsWith(this.domain)) {
                if (qname.equals(this.domain)) {
                    handleSelf(this.domain);
                } else if (this.nameServers.containsKey(qname)) {
                    handleNameservers(qname);
                } else {
                    handleSubdomains(qname);
                }
            } else if ("SOA".equals(qtype) && qname.endsWith(this.domain)) {
                handleSoa(qname);
            } else {
                handleUnknown(qtype, qname);
            }
        }
    }

    private void handleSelf(String name) throws IOException {
        write("DATA", this.bits, this.auth, name, "IN", "A", this.ttl, this.id, this.ipAddress);
        writeNameServers(name);
        writeEnd();
    }

    private void handleSubdomains(String qname) throws IOException {
        String subdomain = qname.substring(0, qname.indexOf(this.domain) - 1);
    
        List<String> subparts = splitSubdomain(subdomain);
        if (subparts.size() < 4) {
            if (isDebug()) {
                log("subparts less than 4");
            }
            handleInvalidIp(qname);
            return;
        }
    
        String ipStr = String.join(".", subparts.subList(subparts.size() - 4, subparts.size()));
        InetAddress ipAddress;
        try {
            ipAddress = InetAddress.getByName(ipStr);
        } catch (UnknownHostException e) {
            handleInvalidIp(qname);
            return;
        }
        if (isDebug()) {
            log("extracted ip: " + ipAddress);
        }
    
        // 使用 SubnetUtils 来处理白名单检查
        boolean isWhitelisted = this.whitelistedRanges.stream().anyMatch(range -> {
            SubnetUtils subnet = new SubnetUtils(range);
            subnet.setInclusiveHostCount(true); // 使网络地址和广播地址都包括在内
            return subnet.getInfo().isInRange(ipAddress.getHostAddress());
        });
    
        if (!isWhitelisted) {
            handleNotWhitelisted(ipAddress.getHostAddress());
            return;
        }
    
        if (this.blacklistedIps.contains(ipAddress.getHostAddress())) {
            handleBlacklisted(ipAddress.getHostAddress());
            return;
        }
    
        handleResolved(ipAddress.getHostAddress(), qname);
    }

    private void handleResolved(String address, String qname) throws IOException {
        write("DATA", this.bits, this.auth, qname, "IN", "A", this.ttl, this.id, address);
        writeNameServers(qname);
        writeEnd();
    }

    private void handleNameservers(String qname) throws IOException {
        String ip = this.nameServers.get(qname);
        write("DATA", this.bits, this.auth, qname, "IN", "A", this.ttl, this.id, ip);
        writeEnd();
    }

    private void writeNameServers(String qname) throws IOException {
        for (String nameServer : this.nameServers.keySet()) {
            write("DATA", this.bits, this.auth, qname, "IN", "NS", this.ttl, this.id, nameServer);
        }
    }

    private void handleSoa(String qname) throws IOException {
        write("DATA", this.bits, this.auth, qname, "IN", "SOA", this.ttl, this.id, this.soa);
        writeEnd();
    }

    private void handleUnknown(String qtype, String qname) throws IOException {
        write("LOG", "Unknown type: " + qtype + ", domain: " + qname);
        writeEnd();
    }

    private void handleNotWhitelisted(String ipAddress) throws IOException {
        write("LOG", "Not Whitelisted: " + ipAddress);
        writeEnd();
    }

    private void handleBlacklisted(String ipAddress) throws IOException {
        write("LOG", "Blacklisted: " + ipAddress);
        writeEnd();
    }

    private void handleInvalidIp(String ipAddress) throws IOException {
        write("LOG", "Invalid IP address: " + ipAddress);
        writeEnd();
    }

    private List<String> splitSubdomain(String subdomain) {
        Pattern pattern = Pattern.compile("(?:^|.*[.-])([0-9A-Fa-f]{8})$");
        Matcher matcher = pattern.matcher(subdomain);
        if (matcher.find()) {
            String s = matcher.group(1);
            return Arrays.asList(
                    String.valueOf(Integer.parseInt(s.substring(0, 2), 16)),
                    String.valueOf(Integer.parseInt(s.substring(2, 4), 16)),
                    String.valueOf(Integer.parseInt(s.substring(4, 6), 16)),
                    String.valueOf(Integer.parseInt(s.substring(6, 8), 16))
            );
        }
        return Arrays.asList(subdomain.split("[.-]"));
    }

    private String resolveConfig(String environmentKey, Properties config, String configKey) {
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null) {
            return environmentValue;
        }
        String configValue = config.getProperty(configKey);
        if (configValue != null) {
            return configValue;
        }
        throw new RuntimeException("Failed to resolve config for environmentKey=" + environmentKey +
                " config section.key=" + configKey);
    }

    private Map<String, String> parseEnvSplitted(String key, Properties config, String configSection) {
        String environmentValue = System.getenv(key);
        Map<String, String> result = new LinkedHashMap<>();

        if (environmentValue != null) {
            for (String value : environmentValue.split(" ")) {
                String[] parts = value.split("=", 2);
                result.put(parts[0], parts[1]);
            }
        } else {
            for (String configKey : config.stringPropertyNames()) {
                if (configKey.startsWith(configSection)) {
                    String value = config.getProperty(configKey);
                    result.put(configKey.replace(configSection + ".", ""), value);
                }
            }
        }

        return result;
    }

    private List<String> getNext() throws IOException {
        if (isDebug()) {
            log("reading now");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (isDebug()) {
            log("read line: " + line);
        }
        return Arrays.asList(line.strip().split("\t"));
    }

    private void writeEnd() throws IOException {
        write("END");
    }

    private void write(String... args) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        writer.write(String.join("\t", args));
        writer.write("\n");
        writer.flush();
    }

    private void log(String msg) {
        System.err.println("backend (" + ProcessHandle.current().pid() + "): " + msg);
    }

    private boolean isDebug() {
        return false;
    }

    private static String getDefaultConfigFile() {
        // Load the resource using the class loader
        return Objects.requireNonNull(DynamicBackend.class.getClassLoader().getResource("backend.conf")).getPath();
    }

    public static void main(String[] args) throws IOException {
        DynamicBackend backend = new DynamicBackend();
        backend.configure(getDefaultConfigFile());
        backend.run();
    }
}
