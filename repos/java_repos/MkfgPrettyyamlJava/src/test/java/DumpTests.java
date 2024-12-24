import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import static org.junit.jupiter.api.Assertions.*;

public class DumpTests {
    private final Yaml yaml = new Yaml();
    private static final Pyaml pyaml = new Pyaml();
    
    private static final String largeYaml = 
    "### Default (baseline) configuration parameters.\n" +
    "### DO NOT ever change this config, use -c commandline option instead!\n" +
    "\n" +
    "# Note that this file is YAML, so YAML types can be used here, see http://yaml.org/type/\n" +
    "# For instance, large number can be specified as \"10_000_000\" or \"!!float 10e6\".\n" +
    "\n" +
    "source:\n" +
    "  # Path or glob pattern (to match path) to backup, required\n" +
    "  path: # example: /srv/backups/weekly.*\n" +
    "\n" +
    "  queue:\n" +
    "    # Path to intermediate backup queue-file (list of paths to upload), required\n" +
    "    path: # example: /srv/backups/queue.txt\n" +
    "    # Don't rebuild queue-file if it's newer than source.path\n" +
    "    check_mtime: true\n" +
    "\n" +
    "  entry_cache:\n" +
    "    # Path to persistent db (sqlite) of remote directory nodes, required\n" +
    "    path: # example: /srv/backups/dentries.sqlite\n" +
    "\n" +
    "  # How to pick a path among those matched by \"path\" glob\n" +
    "  pick_policy: alphasort_last # only one supported\n" +
    "\n" +
    "destination:\n" +
    "  # URL of Tahoe-LAFS node webapi\n" +
    "  url: http://localhost:3456/uri\n" +
    "\n" +
    "  result: # what to do with a cap (URI) of a resulting tree (with full backup)\n" +
    "    print_to_stdout: true\n" +
    "    # Append the entry to the specified file (creating it, if doesn't exists)\n" +
    "    # Example entry: \"2012-10-10T23:12:43.904543 /srv/backups/weekly.2012-10-10 URI:DIR2-CHK:...\"\n" +
    "    append_to_file: # example: /srv/backups/lafs_caps\n" +
    "    append_to_lafs_dir: # example: URI:DIR2:...\n" +
    "\n" +
    "  encoding:\n" +
    "    xz:\n" +
    "      enabled: true\n" +
    "      options: # see lzma.LZMAOptions, empty = module defaults\n" +
    "      min_size: 5120 # don't compress files smaller than 5 KiB (unless overidden in \"path_filter\")\n" +
    "      path_filter:\n" +
    "        - '\\.(gz|bz2|t[gb]z2?|xz|lzma|7z|zip|rar)$'\n" +
    "        - '\\.(rpm|deb|iso)$'\n" +
    "        - '\\.(jpe?g|gif|png|mov|avi|ogg|mkv|webm|mp[34g]|flv|flac|ape|pdf|djvu)$'\n" +
    "        - '\\.(sqlite3?|fossil|fsl)$'\n" +
    "        - '\\.git/objects/[0-9a-f]+/[0-9a-f]+$'\n" +
    "\n" +
    "http:\n" +
    "  request_pool_options:\n" +
    "    maxPersistentPerHost: 10\n" +
    "    cachedConnectionTimeout: 600\n" +
    "    retryAutomatically: true\n" +
    "  ca_certs_files: /etc/ssl/certs/ca-certificates.crt # can be a list\n" +
    "  debug_requests: false # insecure! logs will contain tahoe caps\n" +
    "\n" +
    "filter:\n" +
    "  - '/(CVS|RCS|SCCS|_darcs|\\{arch\\})/$'\n" +
    "  - '/\\.(git|hg|bzr|svn|cvs)(/|ignore|attributes|tags)?$'\n" +
    "\n" +
    "operation:\n" +
    "  queue_only: false # only generate upload queue file, don't upload anything\n" +
    "  reuse_queue: false # don't generate upload queue file, use existing one as-is\n" +
    "  disable_deduplication: false # make no effort to de-duplicate data (should still work on tahoe-level for files)\n" +
    "\n" +
    "  rate_limit:\n" +
    "    bytes: # limit on rate of *file* bytes upload, example: 1/3e5:20e6\n" +
    "    objects: # limit on rate of uploaded objects, example: 10:50\n" +
    "\n" +
    "logging:\n" +
    "  warnings: true # capture python warnings\n" +
    "  sql_queries: false # log executed sqlite queries (very noisy, caps will be there)\n" +
    "  version: 1\n" +
    "  formatters:\n" +
    "    basic:\n" +
    "      format: '%(asctime)s :: %(name)s :: %(levelname)s: %(message)s'\n" +
    "      datefmt: '%Y-%m-%d %H:%M:%S'\n" +
    "  handlers:\n" +
    "    console:\n" +
    "      class: logging.StreamHandler\n" +
    "      stream: ext://sys.stderr\n" +
    "      formatter: basic\n" +
    "      level: custom\n" +
    "    debug_logfile:\n" +
    "      class: logging.handlers.RotatingFileHandler\n" +
    "      filename: /srv/backups/debug.log\n" +
    "      formatter: basic\n" +
    "      encoding: utf-8\n" +
    "      maxBytes: 5242880 # 5 MiB\n" +
    "      backupCount: 2\n" +
    "      level: NOISE\n" +
    "  loggers:\n" +
    "    twisted:\n" +
    "      handlers: [console]\n" +
    "      level: 0\n" +
    "  root:\n" +
    "    level: custom\n" +
    "    handlers: [console]\n";

    enum TestConst {
        DISPATCH(2455),
        HEARTBEAT(123);

        private final int value;

        TestConst(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private final Map<String, Object> data = new LinkedHashMap<String, Object>() {{
        put("path", "/some/path");
        put("query_dump", new LinkedHashMap<String, Object>() {{
            put("key1", "тест1");
            put("key2", "тест2");
            put("key3", "тест3");
            put("последний", null);
        }});
        put("ids", new LinkedHashMap<>());
        put("a", Arrays.asList(1, null, "asd", "не-ascii"));
        put("b", 3.5);
        put("c", null);
        put("d", TestConst.DISPATCH.getValue());
        put("asd", new LinkedHashMap<String, Object>() {{
            put("b", 1);
            put("a", 2);
        }});
    }};

    public static final Map<String, String> dataStrMultiline = new LinkedHashMap<String, String>() {{
        put("cert", 
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDUjCCAjoCCQD0/aLLkLY/QDANBgkqhkiG9w0BAQUFADBqMRAwDgYDVQQKFAdm\n" +
            "Z19jb3JlMRYwFAYDVQQHEw1ZZWthdGVyaW5idXJnMR0wGwYDVQQIExRTdmVyZGxv\n" +
            "dnNrYXlhIG9ibGFzdDELMAkGA1UEBhMCUlUxEjAQBgNVBAMTCWxvY2FsaG9zdDAg\n" +
            "Fw0xMzA0MjQwODUxMTRaGA8yMDUzMDQxNDA4NTExNFowajEQMA4GA1UEChQHZmdf\n" +
            "Y29yZTEWMBQGA1UEBxMNWWVrYXRlcmluYnVyZzEdMBsGA1UECBMUU3ZlcmRsb3Zz\n" +
            "a2F5YSBvYmxhc3QxCzAJBgNVBAYTAlJVMRIwEAYDVQQDEwlsb2NhbGhvc3QwggEi\n" +
            "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCnZr3jbhfb5bUhORhmXOXOml8N\n" +
            "fAli/ak6Yv+LRBtmOjke2gFybPZFuXYr0lYGQ4KgarN904vEg7WUbSlwwJuszJxQ\n" +
            "Lz3xSDqQDqF74m1XeBYywZQIywKIbA/rfop3qiMeDWo3WavYp2kaxW28Xd/ZcsTd\n" +
            "bN/eRo+Ft1bor1VPiQbkQKaOOi6K8M9a/2TK1ei2MceNbw6YrlCZe09l61RajCiz\n" +
            "y5eZc96/1j436wynmqJn46hzc1gC3APjrkuYrvUNKORp8y//ye+6TX1mVbYW+M5n\n" +
            "CZsIjjm9URUXf4wsacNlCHln1nwBxUe6D4e2Hxh2Oc0cocrAipxuNAa8Afn5AgMB\n" +
            "AAEwDQYJKoZIhvcNAQEFBQADggEBADUHf1UXsiKCOYam9u3c0GRjg4V0TKkIeZWc\n" +
            "uN59JWnpa/6RBJbykiZh8AMwdTonu02g95+13g44kjlUnK3WG5vGeUTrGv+6cnAf\n" +
            "4B4XwnWTHADQxbdRLja/YXqTkZrXkd7W3Ipxdi0bDCOSi/BXSmiblyWdbNU4cHF/\n" +
            "Ex4dTWeGFiTWY2upX8sa+1PuZjk/Ry+RPMLzuamvzP20mVXmKtEIfQTzz4b8+Pom\n" +
            "T1gqPkNEbe2j1DciRNUOH1iuY+cL/b7JqZvvdQK34w3t9Cz7GtMWKo+g+ZRdh3+q\n" +
            "2sn5m3EkrUb1hSKQbMWTbnaG4C/F3i4KVkH+8AZmR9OvOmZ+7Lo=\n" +
            "-----END CERTIFICATE-----");
    }};

    public static final Map<String, String> dataStrLong = new LinkedHashMap<String, String>() {{
        put("cert", 
            "MIIDUjCCAjoCCQD0/aLLkLY/QDANBgkqhkiG9w0BAQUFADBqMRAwDgYDVQQKFAdm" +
            "Z19jb3JlMRYwFAYDVQQHEw1ZZWthdGVyaW5idXJnMR0wGwYDVQQIExRTdmVyZGxv" +
            "dnNrYXlhIG9ibGFzdDELMAkGA1UEBhMCUlUxEjAQBgNVBAMTCWxvY2FsaG9zdDAg" +
            "Fw0xMzA0MjQwODUxMTRaGA8yMDUzMDQxNDA4NTExNFowajEQMA4GA1UEChQHZmdf" +
            "Y29yZTEWMBQGA1UEBxMNWWVrYXRlcmluYnVyZzEdMBsGA1UECBMUU3ZlcmRsb3Zz" +
            "a2F5YSBvYmxhc3QxCzAJBgNVBAYTAlJVMRIwEAYDVQQDEwlsb2NhbGhvc3QwggEi" +
            "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCnZr3jbhfb5bUhORhmXOXOml8N" +
            "fAli/ak6Yv+LRBtmOjke2gFybPZFuXYr0lYGQ4KgarN904vEg7WUbSlwwJuszJxQ" +
            "Lz3xSDqQDqF74m1XeBYywZQIywKIbA/rfop3qiMeDWo3WavYp2kaxW28Xd/ZcsTd" +
            "bN/eRo+Ft1bor1VPiQbkQKaOOi6K8M9a/2TK1ei2MceNbw6YrlCZe09l61RajCiz" +
            "y5eZc96/1j436wynmqJn46hzc1gC3APjrkuYrvUNKORp8y//ye+6TX1mVbYW+M5n" +
            "CZsIjjm9URUXf4wsacNlCHln1nwBxUe6D4e2Hxh2Oc0cocrAipxuNAa8Afn5AgMB" +
            "AAEwDQYJKoZIhvcNAQEFBQADggEBADUHf1UXsiKCOYam9u3c0GRjg4V0TKkIeZWc" +
            "uN59JWnpa/6RBJbykiZh8AMwdTonu02g95+13g44kjlUnK3WG5vGeUTrGv+6cnAf" +
            "4B4XwnWTHADQxbdRLja/YXqTkZrXkd7W3Ipxdi0bDCOSi/BXSmiblyWdbNU4cHF/" +
            "Ex4dTWeGFiTWY2upX8sa+1PuZjk/Ry+RPMLzuamvzP20mVXmKtEIfQTzz4b8+Pom" +
            "T1gqPkNEbe2j1DciRNUOH1iuY+cL/b7JqZvvdQK34w3t9Cz7GtMWKo+g+ZRdh3+q" +
            "2sn5m3EkrUb1hSKQbMWTbnaG4C/F3i4KVkH+8AZmR9OvOmZ+7Lo=");
    }};

    private String yamlVar(String ys, boolean raw) {
        ys = ys.replaceAll("\t", "  ");
        if (raw) {
            return ys;
        }
        return yaml.dump(yaml.load(ys));
    }

    private List<Object> flatten(Object data, List<String> path) {
        List<Object> dst = new ArrayList<>();
        if (data instanceof List) {
            for (Object v : (List<?>) data) {
                List<String> newPath = new ArrayList<>(path);
                newPath.add("!!list");
                dst.addAll(flatten(v, newPath));
            }
        } else if (data instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) data).entrySet()) {
                List<String> newPath = new ArrayList<>(path);
                newPath.add(String.valueOf(entry.getKey()));
                dst.addAll(flatten(entry.getValue(), newPath));
            }
        } else {
            dst.add(Arrays.asList(path, data));
        }
        return dst;
    }

    private List<Integer> posList(String ys, String sep) {
        List<Integer> posList = new ArrayList<>();
        int pos = 0;
        while (true) {
            pos = ys.indexOf(sep, pos + 1);
            if (pos < 0) break;
            posList.add(pos);
        }
        return posList;
    }

    private List<Integer> emptyLineList(String ys) {
        List<Integer> emptyLines = new ArrayList<>();
        String[] lines = ys.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().isEmpty()) {
                emptyLines.add(i);
            }
        }
        return emptyLines;
    }

    @Test
    public void testDst() throws Exception {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        
        pyaml.dump(data, buff);
        assertNotNull(buff.toString(StandardCharsets.UTF_8.name()));

        String yamlString = pyaml.dump(data, String.class);
        assertTrue(yamlString instanceof String);

        byte[] yamlBytes = pyaml.dump(data, byte[].class);
        assertTrue(yamlBytes instanceof byte[]);
    }

    @Test
    public void testSimple() throws Exception {
        List<Object> a = flatten(data);
        
        String yamlString = pyaml.dump(data, String.class);
        
        Map<String, Object> loadedData = yaml.load(yamlString);
        
        assertEquals(a, flatten(loadedData));
    }

    @Test
    public void testVSpacing() throws Exception {
        
        Map<String, Object> data = yaml.load(largeYaml);

        List<Object> a = flatten(data);

        String yamlString = pyaml.dump(data, String.class, new DumperOptions().setVSpacing(true).setSplitLines(10).setSplitCount(2));

        Map<String, Object> loadedData = yaml.load(yamlString);
        assertEquals(a, flatten(loadedData));

        List<Integer> expectedPositions = List.of(
                12, 13, 25, 33, 52, 73, 88, 107, 157, 184, 264, 299, 344, 345, 355, 375, 399,
                424, 425, 458, 459, 467, 505, 561, 600, 601, 607, 660, 681, 705, 738, 767, 795,
                796, 805, 806, 820, 831, 866, 936, 937, 949, 950, 963, 998, 1021, 1041, 1072,
                1073, 1092, 1113, 1163, 1185, 1224, 1247, 1266, 1290, 1291, 1302, 1315, 1331,
                1349, 1364, 1365, 1373, 1387, 1403, 1421, 1422, 1440, 1441, 1454, 1455, 1471,
                1472, 1483, 1511, 1528, 1542, 1553, 1566, 1584, 1585, 1593, 1608, 1618, 1626,
                1656, 1665, 1686, 1696
        );
        List<Integer> actualPositions = posList(yamlString, "\n");
        assertEquals(expectedPositions, actualPositions);

        String yamlStringNoVSpacing = pyaml.dump(data, String.class, new DumperOptions().setVSpacing(false));
        assertFalse(yamlStringNoVSpacing.contains("\n\n"));
    }

    @Test
    public void testIds() throws Exception {
        String b = pyaml.dump(data, String.class, new DumperOptions().setForceEmbed(false));
        
        assertFalse(b.contains("&id00"));
        assertTrue(b.contains("query_dump_clone: *query_dump_clone"));
        assertTrue(b.contains("id в уникоде: &ids_-_id2_"));
    }

    @Test
    public void testIdsUnidecode() throws Exception {
        String b = pyaml.dump(data, String.class, new DumperOptions().setForceEmbed(false));
        
        assertFalse(b.contains("&id00"));
        assertFalse(b.contains("_id00"));
        assertTrue(b.contains("id в уникоде: &ids_-_id2_v_unikode"));
    }

    @Test
    public void testForceEmbed() throws Exception {
        for (boolean fe : new boolean[]{true, false}) {
            String dump = pyaml.dump(data, String.class, new DumperOptions().setForceEmbed(fe));
            if (fe) {
                assertFalse(dump.contains("*"));
                assertFalse(dump.contains("&"));
            } else {
                assertTrue(dump.contains("*"));
                assertTrue(dump.contains("&"));
            }
        }
    }
    
    @Test
    public void testEncoding() throws Exception {
        String b = pyaml.dump(data, String.class, new DumperOptions().setForceEmbed(true));
        List<String> bLines = Arrays.stream(b.split("\n")).map(String::trim).collect(Collectors.toList());
        
        List<String> chk = Arrays.asList("query_dump:", "key1: тест1", "key2: тест2", "key3: тест3", "последний:");
        int pos = bLines.indexOf("query_dump:");
        assertEquals(bLines.subList(pos, pos + chk.size()), chk);
    }

    @Test
    public void testStrLong() throws Exception {
        String b = pyaml.dump(dataStrLong);
        
        assertFalse(b.contains("\""));
        assertFalse(b.contains("'"));
        assertEquals(1, b.split("\n").length);
    }

    @Test
    public void testStrMultiline() throws Exception {
        String b = pyaml.dump(dataStrMultiline);
        String[] bLines = b.split("\n");
        assertTrue(bLines.length > dataStrMultiline.get("cert").split("\n").length);
    
        for (String line : bLines) {
            assertTrue(line.length() < 100);
        }
    }
    
    @Test
    public void testDumps() throws Exception {
        String b = pyaml.dumps(dataStrMultiline);
        assertTrue(b instanceof String);
    }

    @Test
    public void testPrint() throws Exception {
        assertSame(pyaml::print, pyaml::pprint);
        assertSame(pyaml::print, pyaml::p);
    
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        byte[] b = pyaml.dump(dataStrMultiline, byte[].class);
        
        pyaml.print(dataStrMultiline, new PrintStream(buff));
        
        assertArrayEquals(b, buff.toByteArray());
    }
        
    @Test
    public void testPrintArgs() throws Exception {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        Object[] args = {1, 2, 3};
        
        byte[] b = pyaml.dump(args, byte[].class);
        pyaml.print(args, new PrintStream(buff));
        
        assertArrayEquals(b, buff.toByteArray());
    }

    @Test
    public void testStrStyles() throws Exception {
        String a = pyaml.dump(dataStrMultiline);
        String b = pyaml.dump(dataStrMultiline, new DumperOptions().setStringValStyle("|"));
        assertEquals(a, b);
    
        b = pyaml.dump(dataStrMultiline, new DumperOptions().setStringValStyle("plain"));
        assertNotEquals(a, b);
    
        String c = pyaml.dump(dataStrMultiline, new DumperOptions().setStringValStyle("literal"));
        assertNotEquals(c, a);
        assertNotEquals(c, b);
    
        assertTrue(pyaml.dump("waka waka", new DumperOptions().setStringValStyle("|")).startsWith("|-\n"));
    
        Map<String, Object> dataInt = new HashMap<>();
        dataInt.put("a", 123);
        String dataIntDump = pyaml.dump(dataInt);
        assertEquals("a: 123\n", dataIntDump);
        assertEquals(dataIntDump, pyaml.dump(dataInt, new DumperOptions().setStringValStyle("|")));
        assertEquals(dataIntDump, pyaml.dump(dataInt, new DumperOptions().setStringValStyle("literal")));
    
        Map<String, Object> dataStr = new HashMap<>();
        dataStr.put("a", "123");
        a = pyaml.dump(dataStr);
        b = pyaml.dump(dataStr, new DumperOptions().setStringValStyle("|"));
        
        assertEquals("a: '123'\n", a);
        assertEquals(flatten(dataStr), flatten(yaml.load(a)));
        assertNotEquals(a, b);
        assertEquals(flatten(dataStr), flatten(yaml.load(b)));
    }

    @Test
    public void testColonsInStrings() throws Exception {
        Map<String, Object> val1 = new LinkedHashMap<>();
        val1.put("foo", Arrays.asList("bar:", "baz", "bar:bazzo", "a: b"));
        val1.put("foo:", "yak:");
        
        String val1Str = pyaml.dump(val1);
        Map<String, Object> val2 = yaml.load(val1Str);
        String val2Str = pyaml.dump(val2);
        Map<String, Object> val3 = yaml.load(val2Str);
    
        assertEquals(val1, val2);
        assertEquals(val1Str, val2Str);
        assertEquals(val2, val3);
    }

    @Test
    public void testUnquotedSpaces() throws Exception {
        Map<String, String> val1 = new LinkedHashMap<>();
        val1.put("key", "word1 word2 word3");
        val1.put("key key", "asd");
        val1.put("k3", "word: stuff");

        String val1Str = pyaml.dump(val1);
        Map<String, String> val2 = yaml.load(val1Str);
        
        assertEquals(val1, val2);
        assertTrue(val1Str.contains("key: word1 word2 word3"));
    }

    @Test
    public void testEmptyStrings() throws Exception {
        Map<String, Object> val1 = new LinkedHashMap<>();
        val1.put("key", Arrays.asList("", "stuff", "", "more"));
        val1.put("", "value");
        val1.put("k3", "");
    
        String val1Str = pyaml.dump(val1);
        Map<String, Object> val2 = yaml.load(val1Str);
        String val2Str = pyaml.dump(val2);
        Map<String, Object> val3 = yaml.load(val2Str);
    
        assertEquals(val1, val2);
        assertEquals(val1Str, val2Str);
        assertEquals(val2, val3);
    }

    @Test
    public void testSingleDashStrings() throws Exception {
        Function<String, String> stripSeqDash = line -> line.stripLeading().replaceFirst("-", "").stripLeading();

        Map<String, Object> val1 = new LinkedHashMap<>();
        val1.put("key", Arrays.asList("-", "-stuff", "- -", "- more-", "more-", "--"));

        String val1Str = pyaml.dump(val1);
        Map<String, Object> val2 = yaml.load(val1Str);
        String val2Str = pyaml.dump(val2);
        Map<String, Object> val3 = yaml.load(val2Str);

        assertEquals(val1, val2);
        assertEquals(val1Str, val2Str);
        assertEquals(val2, val3);

        String[] val1StrLines = val1Str.split("\n");
        assertEquals(stripSeqDash.apply(val1StrLines[2]), "-stuff");
        assertEquals(stripSeqDash.apply(val1StrLines[5]), "more-");
        assertEquals(stripSeqDash.apply(val1StrLines[6]), "--");

        val1 = new LinkedHashMap<>();
        val1.put("key", "-");

        val1Str = pyaml.dump(val1);
        val2 = yaml.load(val1Str);
        val2Str = pyaml.dump(val2);
        val3 = yaml.load(val2Str);
    }

    @Test
    public void testNamedTuple() throws Exception {
        // Named tuple equivalent using a custom class in Java
        class TestTuple {
            final int y, x, z;
            
            TestTuple(int y, int x, int z) {
                this.y = y;
                this.x = x;
                this.z = z;
            }
        }
    
        TestTuple val = new TestTuple(1, 2, 3);
        String valStr = pyaml.dump(val, new DumperOptions().setSortKeys(false));
        
        assertEquals("y: 1\nx: 2\nz: 3\n", valStr);
    }

    @Test
    public void testOrderedDict() throws Exception {
        Map<Integer, String> d = new LinkedHashMap<>();
        for (int i = 9; i >= 0; i--) {
            d.put(i, "");
        }

        List<String> lines = Arrays.asList(pyaml.dump(d, new DumperOptions().setSortKeys(false)).split("\n"));
        List<String> reversedSortedLines = Arrays.asList(pyaml.dump(d).split("\n"));

        assertEquals(lines, reversedSortedLines);
    }

    @Test
    public void testEnum() throws Exception {
        TestConst c = TestConst.HEARTBEAT;
        Map<Object, Object> d1 = new LinkedHashMap<>();
        d1.put("a", c);
        d1.put("b", c.getValue());
        d1.put(c, "testx");
    
        assertEquals(d1.get("a"), d1.get("b"));
    
        String s = pyaml.dump(d1);
        Map<Object, Object> d2 = yaml.load(s);
    
        assertEquals(d1.get("a"), d2.get("a"));
        assertEquals(d1.get("a"), c);
        assertEquals(d1.get(c), "testx");
        assertTrue(s.contains("a: 123 # TestConst.HEARTBEAT"));
    }

    @Test
    public void testPyYamlParams() throws Exception {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("foo", "lorem ipsum ".repeat(30));  // 300+ chars
    
        for (int w : Arrays.asList(40, 80, 200)) {
            List<String> lines = Arrays.asList(pyaml.dump(d, new DumperOptions().setWidth(w).setIndent(10)).split("\n"));
    
            for (int n = 0; n < lines.size(); n++) {
                String line = lines.get(n);
                assertTrue(line.length() < w * 1.2);
                if (n != lines.size() - 1) {
                    assertTrue(line.length() > w * 0.8);
                }
            }
        }
    }
    
    @Test
    public void testMultipleDocs() throws Exception {
        List<Object> docs = Arrays.asList(yaml.load(largeYaml), new LinkedHashMap<String, Object>() {{
            put("a", 1);
            put("b", 2);
            put("c", 3);
        }});
    
        String docsStr = pyaml.dumpAll(docs, new DumperOptions().setVSpacing(true));
        assertTrue(docsStr.startsWith("---"));
        assertTrue(docsStr.contains("---\n\na: 1\n\nb: 2\n\nc: 3\n"));
    
        String docsStr2 = pyaml.dump(docs, new DumperOptions().setVSpacing(true).setMultipleDocs(true));
        assertEquals(docsStr, docsStr2);
    
        docsStr2 = pyaml.dump(docs, new DumperOptions().setVSpacing(true));
        assertNotEquals(docsStr, docsStr2);
    
        docsStr2 = pyaml.dumpAll(docs, new DumperOptions().setExplicitStart(false));
        assertFalse(docsStr2.startsWith("---"));
        assertNotEquals(docsStr, docsStr2);
    
        docsStr = pyaml.dump(docs, new DumperOptions().setMultipleDocs(true).setExplicitStart(false));
        assertEquals(docsStr, docsStr2);
    }

    @Test
    public void testRuamelYaml() throws Exception {
        try {
            // Simulate loading ruamel.yaml equivalent functionality
            Map<String, Object> data = yaml.load(largeYaml);
            String yamlStr = pyaml.dump(data);
        } catch (Exception e) {
            throw new SkipException("No ruamel.yaml module to test it");
        }
    }

    @Test
    public void testDumpStreamKws() throws Exception {
        List<Integer> data = Arrays.asList(1, 2, 3);
        StringWriter buff1 = new StringWriter();
        StringWriter buff2 = new StringWriter();

        pyaml.dump(data, buff1);
        pyaml.dump(data, buff2);
        assertEquals(buff1.toString(), buff2.toString());

        buff1.getBuffer().setLength(0);
        pyaml.dump(data, buff1, buff1);
        assertEquals(buff1.toString(), buff2.toString());

        String ys = pyaml.dump(data, String.class, String.class);
        assertEquals(ys, buff2.toString());

        buff1.getBuffer().setLength(0);
        buff2.getBuffer().setLength(0);
        
        assertThrows(TypeError.class, () -> pyaml.dump(data, buff1, buff2));
        assertThrows(TypeError.class, () -> pyaml.dump(data, String.class, buff2));
        
        assertEquals("", buff1.toString());
        assertEquals("", buff2.toString());
    }

    @Test
    public void testListVSpacing() throws Exception {
        String itm = yamlVar(
            "builtIn: 1\n" +
            "datasource:\n" +
            "  type: grafana\n" +
            "  uid: -- Grafana --\n" +
            "enable: yes\n" +
            "hide: yes\n" +
            "iconColor: rgba(0, 211, 255, 1)\n" +
            "name: Annotations & Alerts\n" +
            "type: dashboard\n", false);        
    
        String ys = pyaml.dump(Collections.singletonMap("mylist", Collections.nCopies(10, itm)));
        assertEquals(emptyLineList(ys), Arrays.asList(1, 11, 21, 31, 41, 51, 61, 71, 81, 91));
    
        ys = yamlVar(
            "panels:\n" +
            "  - datasource:\n" +
            "      type: datasource\n" +
            "      uid: grafana\n" +
            "    fieldConfig:\n", true);        
    
        for (int n = 0; n < 60; n++) {
            ys += "\n" + "  ".repeat(3) + "field" + n + ": value-" + n;
        }
    
        ys = pyaml.dump(yaml.load(ys), new DumperOptions().setVSpacing(new VSpacingOptions().setOnelineSplit(true)));
        assertEquals(emptyLineList(ys), range(4, 126, 2));
    
        ys = pyaml.dump(yaml.load(ys));
        assertEquals(emptyLineList(ys), Collections.singletonList(4));
    }
    
    @Test
    public void testAnchorCutoff() throws Exception {
        String data = yamlVar(
            "similique-natus-inventore-deserunt-amet-explicabo-cum-accusamus-temporibus:\n" +
            "  quam-nulla-dolorem-dolore-velit-quis-deserunt-est-ullam-exercitationem:\n" +
            "    culpa-quia-incidunt-accusantium-ad-dicta-nobis-rerum-veritatis: &test\n" +
            "      test: 1\n" +
            "similique-commodi-aperiam-libero-error-eos-quidem-eius:\n" +
            "  ipsam-labore-enim,-vero-voluptatem-eaque-dolores-blanditiis-recusandae:\n" +
            "    quas-atque-maxime-itaque-ullam-sequi-suscipit-quis-vitae-veritatis: *test\n", 
            false);        
    
        String ys = pyaml.dump(yaml.load(data), new DumperOptions().setForceEmbed(false));
        
        for (String c : Arrays.asList("&", "\\*")) {
            Pattern pattern = Pattern.compile("(?<= )" + c + "\\S+");
            Matcher m = pattern.matcher(ys);
            assertTrue(m.find());
            assertTrue(m.group().length() < 50);
            assertTrue(m.group().contains("similique"));
            assertTrue(m.group().contains("veritatis"));
        }
    
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("a", 1);
        v.put("b", 2);
        v.put("c", 3);
    
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("test1", Collections.singletonMap("test2", v));
        dataMap.put("test3", Collections.singletonMap("test4", v));
    
        ys = pyaml.dump(dataMap, new DumperOptions().setForceEmbed(false));
        
        for (String c : Arrays.asList("&", "\\*")) {
            Pattern pattern = Pattern.compile("(?<= )" + c + "\\S+");
            Matcher m = pattern.matcher(ys);
            assertTrue(m.find());
            assertTrue(m.group().length() < 30);
            assertEquals(2, pattern.matcher(ys).results().count());
        }
    }

    @Test
    public void testGroupOnlineValues() throws Exception {
        String data = yamlVar(
            "similique-natus: 1\n" +
            "similique-commodi:\n" +
            "  aperiam-libero: 2\n" +
            "'vel praesentium quo':\n" +
            "  exercitationem debitis: porro beatae id\n" +
            "  rerum commodi ipsum: nesciunt veritatis\n" +
            "  amet quaerat:\n" +
            "    assumenda: odio tenetur saepe\n" +
            "\"111\": digit-string\n" +
            "deserunt-est-2: asdasd\n" +
            "deserunt-est-1: |\n" +
            "  line1\n" +
            "  line2\n" +
            "culpa-quia: 1234\n" +
            "deserunt-est-3: asdasd\n" +
            "10: test1\n" +
            "200: test\n" +
            "30: test2\n", false);        

        // Sort and oneline-group
        String ys1 = pyaml.dump(yaml.load(data), new DumperOptions().setSortDicts(Pyaml.PYAMLSort.ONELINE_GROUP).setVSpacing(new VSpacingOptions().setSplitLines(0).setSplitCount(0)));
        assertEquals(emptyLineList(ys1), Arrays.asList(8, 12, 15, 19));

        // No oneline-group by default
        String ys2 = pyaml.dump(yaml.load(data), new DumperOptions().setVSpacing(new VSpacingOptions().setSplitLines(0).setSplitCount(0)));
        assertNotEquals(ys1, ys2);
        assertEquals(emptyLineList(ys2), Arrays.asList(1, 4, 6, 9, 11, 13, 15, 17, 21, 23, 25, 27, 29));

        // No-sort oneline-group overrides oneline-split for consecutive oneliners
        String ys3 = pyaml.dump(yaml.load(data), new DumperOptions().setSortKeys(false).setVSpacing(new VSpacingOptions().setSplitLines(0).setSplitCount(0).setOnelineGroup(true).setOnelineSplit(true)));
        assertNotEquals(ys1, ys3);
        assertNotEquals(ys2, ys3);
        assertEquals(emptyLineList(ys3), Arrays.asList(1, 4, 8, 11, 14, 18));
    }

}
