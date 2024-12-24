import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;
import java.util.regex.Pattern;

public class TestAnonip {

    private Anonip anonip;

    @BeforeEach
    public void setUp() {
        anonip = new Anonip(List.of(1), 12, 84, 0, " ", null, null, false);
    }

    // 提供IP测试数据
    private static Stream<Arguments> provideIpMaskData() {
        return Stream.of(
            Arguments.of("192.168.100.200", 12, 84, "192.168.96.0"),
            Arguments.of("192.168.100.200:80", 12, 84, "192.168.96.0:80"),
            Arguments.of("192.168.100.200]", 12, 84, "192.168.96.0]"),
            Arguments.of("192.168.100.200:80]", 12, 84, "192.168.96.0:80]"),
            Arguments.of("192.168.100.200", 0, 84, "192.168.100.200"),
            Arguments.of("192.168.100.200", 4, 84, "192.168.100.192"),
            Arguments.of("192.168.100.200", 8, 84, "192.168.100.0"),
            Arguments.of("192.168.100.200", 24, 84, "192.0.0.0"),
            Arguments.of("192.168.100.200", 32, 84, "0.0.0.0"),
            Arguments.of("no_ip_address", 12, 84, "no_ip_address"),
            Arguments.of("2001:0db8:85a3:0000:0000:8a2e:0370:7334", 12, 84, "2001:db8:85a0::"),
            Arguments.of("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]:443", 12, 84, "[2001:db8:85a0::]:443"),
            Arguments.of("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]", 12, 84, "[2001:db8:85a0::]"),
            Arguments.of("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]]", 12, 84, "[2001:db8:85a0::]]"),
            Arguments.of("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]:443]", 12, 84, "[2001:db8:85a0::]:443]"),
            Arguments.of("2001:0db8:85a3:0000:0000:8a2e:0370:7334", 12, 0, "2001:db8:85a3::8a2e:370:7334"),
            Arguments.of("2001:0db8:85a3:0000:0000:8a2e:0370:7334", 12, 4, "2001:db8:85a3::8a2e:370:7330"),
            Arguments.of("2001:0db8:85a3:0000:0000:8a2e:0370:7334", 12, 8, "2001:db8:85a3::8a2e:370:7300"),
            Arguments.of("2001:0db8:85a3:0000:0000:8a2e:0370:7334", 12, 24, "2001:db8:85a3::8a2e:300:0"),
            Arguments.of("2001:0db8:85a3:0000:0000:8a2e:0370:7334", 12, 32, "2001:db8:85a3::8a2e:0:0"),
            Arguments.of("2001:0db8:85a3:0000:0000:8a2e:0370:7334", 12, 62, "2001:db8:85a3::"),
            Arguments.of("2001:0db8:85a3:0000:0000:8a2e:0370:7334", 12, 128, "::"),
            Arguments.of("[2001:db8:1::ab9:C0A8:102]:8080", 12, 84, "[2001:db8::]:8080"),
            Arguments.of("   foo", 12, 84, "   foo")
        );
    }

    @ParameterizedTest
    @MethodSource("provideIpMaskData")
    public void testProcessLine(String ip, int v4mask, int v6mask, String expected) {
        anonip = new Anonip(List.of(1), v4mask, v6mask, 0, " ", null, null, false);
        assertEquals(expected, anonip.processLine(ip));
    }

    // 提供增量测试数据
    private static Stream<Arguments> provideIncrementData() {
        return Stream.of(
            Arguments.of("192.168.100.200", 3, "192.168.96.3"),
            Arguments.of("192.168.100.200", 284414028745874325L, "192.168.96.0")
        );
    }

    @ParameterizedTest
    @MethodSource("provideIncrementData")
    public void testIncrement(String ip, long increment, String expected) {
        anonip = new Anonip(List.of(1), 12, 84, increment, " ", null, null, false);
        assertEquals(expected, anonip.processLine(ip));
    }

    // 提供列测试数据
    private static Stream<Arguments> provideColumnData() {
        return Stream.of(
            Arguments.of("192.168.100.200 some string with öéäü", null, "192.168.96.0 some string with öéäü"),
            Arguments.of("some 192.168.100.200 string with öéäü", List.of(2), "some 192.168.96.0 string with öéäü"),
            Arguments.of("some string 192.168.100.200 with öéäü", List.of(3), "some string 192.168.96.0 with öéäü"),
            Arguments.of("192.168.100.200 192.168.11.222 192.168.123.234", List.of(1, 2, 3), "192.168.96.0 192.168.0.0 192.168.112.0"),
            Arguments.of("192.168.100.200 192.168.11.222 192.168.123.234", List.of(9999), "192.168.100.200 192.168.11.222 192.168.123.234")
        );
    }

    @ParameterizedTest
    @MethodSource("provideColumnData")
    public void testColumn(String line, List<Integer> columns, String expected) {
        anonip = new Anonip(columns, 12, 84, 0, " ", null, null, false);
        assertEquals(expected, anonip.processLine(line));
    }

    // 提供正则表达式测试数据
    private static Stream<Arguments> provideRegexData() {
        return Stream.of(
            Arguments.of(
                "3.3.3.3 - - [20/May/2015:21:05:01 +0000] \"GET / HTTP/1.1\" 200 13358 \"-\" \"useragent\"",
                Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)"),
                "3.3.0.0 - - [20/May/2015:21:05:01 +0000] \"GET / HTTP/1.1\" 200 13358 \"-\" \"useragent\"",
                null
            ),
            Arguments.of(
                "blabla/ 3.3.3.3 /blublu",
                Pattern.compile("^blabla/ ([^,]+) /blublu"),
                "blabla/ 3.3.0.0 /blublu",
                null
            ),
            Arguments.of(
                "1.1.1.1 - somefixedstring: 2.2.2.2 - some random stuff - 3.3.3.3",
                Pattern.compile("^([^,]+) - somefixedstring: ([^,]+) - .* - ([^,]+)"),
                "1.1.0.0 - somefixedstring: 2.2.0.0 - some random stuff - 3.3.0.0",
                null
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideRegexData")
    public void testRegex(String line, Pattern regex, String expected, String replace) {
        anonip = new Anonip(null, 12, 84, 0, " ", replace, regex, false);
        assertEquals(expected, anonip.processLine(line));
    }

    @Test
    public void testReplace() {
        Anonip anonipReplace = new Anonip(null, 12, 84, 0, " ", "replacement", null, false);
        assertEquals("replacement something", anonipReplace.processLine("bla something"));
    }

    @Test
    public void testDelimiter() {
        Anonip anonipDelimiter = new Anonip(null, 12, 84, 0, ";", null, null, false);
        assertEquals("192.168.96.0;some;string;with;öéäü", anonipDelimiter.processLine("192.168.100.200;some;string;with;öéäü"));
    }

    @Test
    public void testSkipPrivate() {
        Anonip anonipSkipPrivate = new Anonip(null, 12, 84, 0, " ", null, null, true);
        assertEquals("192.168.100.200", anonipSkipPrivate.processLine("192.168.100.200"));
    }

    @Test
    public void testRun() throws IOException {
        Anonip anonipRun = new Anonip(null, 12, 84, 0, " ", null, null, false);
        String input = "192.168.100.200\n1.2.3.4\n  \n9.8.130.6\n";
        BufferedReader reader = new BufferedReader(new StringReader(input));
        List<String> lines = anonipRun.run(reader);
        assertEquals(List.of("192.168.96.0", "1.2.0.0", "", "9.8.128.0"), lines);
    }

    @Test
    public void testRunWithInputFile() throws IOException {
        Anonip anonipRunFile = new Anonip(null, 12, 84, 0, " ", null, null, false);
        String inputFile = "192.168.100.200\n1.2.3.4\n  \n9.8.130.6\n";
        BufferedReader reader = new BufferedReader(new StringReader(inputFile));
        List<String> lines = anonipRunFile.run(reader);
        assertEquals(List.of("192.168.96.0", "1.2.0.0", "", "9.8.128.0"), lines);
    }

    // 提供CLI参数测试数据
    private static Stream<Arguments> provideCliArgsData() {
        return Stream.of(
            Arguments.of(new String[]{"-c", "3", "5"}, "columns", List.of(3, 5)),
            Arguments.of(new String[]{"-4", "24"}, "ipv4mask", 24),
            Arguments.of(new String[]{"-6", "64"}, "ipv6mask", 64)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCliArgsData")
    public void testCliGenericArgs(String[] args, String attribute, Object expected) {
        anonip = new Anonip();
        anonip.parseArguments(args);
        assertEquals(expected, anonip.getAttribute(attribute));
    }

    // 测试CLI参数混淆
    @Test
    public void testCliArgsAmbiguity() {
        String[] args = {"--regex", "test", "-c", "3"};
        anonip = new Anonip();
        assertThrows(IllegalArgumentException.class, () -> anonip.parseArguments(args));
    }

    // 提供CLI参数混淆测试数据
    private static Stream<Arguments> provideCliArgsAmbiguityData() {
        return Stream.of(
            Arguments.of(new String[]{}, true),
            Arguments.of(new String[]{"--regex", "test"}, true),
            Arguments.of(new String[]{"-c", "4"}, true),
            Arguments.of(new String[]{"--regex", "test", "-c", "3"}, false),
            Arguments.of(new String[]{"--regex", "test", "-l", ";"}, false),
            Arguments.of(new String[]{"--regex", "test", "-l", ";", "-c", "4"}, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCliArgsAmbiguityData")
    public void testCliArgsAmbiguity(String[] args, boolean success) {
        anonip = new Anonip();
        if (success) {
            anonip.parseArguments(args);
        } else {
            assertThrows(IllegalArgumentException.class, () -> anonip.parseArguments(args));
        }
    }

    // 提供正则表达式拼接测试数据
    private static Stream<Arguments> provideRegexConcatData() {
        return Stream.of(
            Arguments.of(new String[]{"--regex", "test"}, "test"),
            Arguments.of(new String[]{"--regex", "foo", "bar", "baz"}, "foo|bar|baz")
        );
    }

    @ParameterizedTest
    @MethodSource("provideRegexConcatData")
    public void testRegexConcat(String[] args, String expected) {
        anonip = new Anonip();
        anonip.parseArguments(args);
        assertEquals(Pattern.compile(expected).toString(), anonip.getRegex().toString());
    }

    // 提供IP掩码验证测试数据
    private static Stream<Arguments> provideValidateIpMaskData() {
        return Stream.of(
            Arguments.of("1", true, 32),
            Arguments.of("0", false, 32),
            Arguments.of("33", false, 32),
            Arguments.of("string", false, 32),
            Arguments.of("129", false, 128)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidateIpMaskData")
    public void testValidateIpMask(String value, boolean valid, int bits) {
        if (valid) {
            assertEquals(Integer.parseInt(value), anonip.validateIpMask(value, bits));
        } else {
            assertThrows(IllegalArgumentException.class, () -> anonip.validateIpMask(value, bits));
        }
    }

    // 提供整数大于0的验证测试数据
    private static Stream<Arguments> provideValidateIntegerHt0Data() {
        return Stream.of(
            Arguments.of("1", true),
            Arguments.of("0", false),
            Arguments.of("-1", false),
            Arguments.of("string", false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidateIntegerHt0Data")
    public void testValidateIntegerHt0(String value, boolean valid) {
        if (valid) {
            assertEquals(Integer.parseInt(value), anonip.validateIntegerHt0(value));
        } else {
            assertThrows(IllegalArgumentException.class, () -> anonip.validateIntegerHt0(value));
        }
    }

    // 提供正则表达式类型参数测试数据
    private static Stream<Arguments> provideRegexArgTypeData() {
        return Stream.of(
            Arguments.of("valid (.*)", true),
            Arguments.of("\\9", false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideRegexArgTypeData")
    public void testRegexArgType(String value, boolean valid) {
        if (valid) {
            assertEquals(value, anonip.regexArgType(value));
        } else {
            assertThrows(IllegalArgumentException.class, () -> anonip.regexArgType(value));
        }
    }

    // 主程序测试（测试是否记录日志以及命令行参数的处理）
    @ParameterizedTest
    @MethodSource("provideMainTestData")
    public void testMain(boolean toFile, boolean debug, int logLevel) throws IOException {
        File logFile = File.createTempFile("anonip", "log");
        logFile.deleteOnExit();

        String[] sysArgs = {
            "-c", "2", "-4", "12", "-6", "42", "-i", "1", "-l", ";", "-r", "replace", "-p"
        };
        if (toFile) {
            sysArgs = append(sysArgs, new String[]{"-o", logFile.getAbsolutePath()});
        }
        if (debug) {
            sysArgs = append(sysArgs, new String[]{"-d"});
        }

        anonip = new Anonip();
        anonip.main(sysArgs);

        if (toFile) {
            List<String> lines = Files.readAllLines(logFile.toPath());
            assertEquals(List.of("string;192.168.100.200", "string;1.2.0.1", "string;2001:db8:85a3::8a2e:370:7334", "string;2a00:1450:400a:803::1", "string;replace"), lines);
        } else {
            // Capture standard output
        }

        Logger logger = Logger.getLogger("anonip");
        assertEquals(logLevel, logger.getLevel().intValue());
    }

    // 测试从输入文件读取
    @Test
    public void testMainReadingFromInputFile() throws IOException {
        File inputFile = File.createTempFile("anonip-input", "txt");
        Files.write(inputFile.toPath(), List.of(
            "192.168.100.200 string",
            "1.2.3.4 string",
            "2001:0db8:85a3:0000:0000:8a2e:0370:7334 string",
            "2a00:1450:400a:803::200e string"
        ));
        inputFile.deleteOnExit();

        anonip = new Anonip();
        anonip.main(new String[]{"--input", inputFile.getAbsolutePath(), "-d"});

        // Check output (simulate captured output as done in Python)
    }

    @Test
    public void testMainReadingFromInputFile() throws IOException {
        // 创建临时文件来模拟输入文件
        File inputFile = File.createTempFile("anonip-input", ".txt");
        inputFile.deleteOnExit();

        // 写入测试数据
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("192.168.100.200 string\n");
            writer.write("1.2.3.4 string\n");
            writer.write("2001:0db8:85a3:0000:0000:8a2e:0370:7334 string\n");
            writer.write("2a00:1450:400a:803::200e string\n");
        }

        // 设置命令行参数，模拟 `sys.argv`
        String[] sysArgs = {"--input", inputFile.getAbsolutePath(), "-d"};

        // 初始化 Anonip 对象并运行主程序
        Anonip anonip = new Anonip();
        anonip.main(sysArgs);

        // 获取输出文件的内容或者模拟输出的捕获
        BufferedReader reader = Files.newBufferedReader(inputFile.toPath());
        List<String> lines = Files.readAllLines(inputFile.toPath());

        // 期望的输出
        List<String> expectedLines = List.of(
            "192.168.96.0 string",
            "1.2.0.0 string",
            "2001:db8:85a0:: string",
            "2a00:1450:4000:: string"
        );

        // 验证输出结果是否符合预期
        assertEquals(expectedLines, lines);
    }

    // 测试prefix字典
    @Test
    public void testPrefixesDict() {
        anonip = new Anonip(11, 83);
        assertEquals(2, anonip.getPrefixes().size());
        assertTrue(anonip.getPrefixes().containsKey(4));
        assertTrue(anonip.getPrefixes().containsKey(6));
    }

    // 测试IPv4和IPv6的属性
    @Test
    public void testPropertiesV4AndV6() {
        anonip = new Anonip(11, 83);
        assertEquals(11, anonip.getIpv4Mask());
        assertEquals(21, anonip.getPrefixes().get(4).intValue());

        assertEquals(83, anonip.getIpv6Mask());
        assertEquals(45, anonip.getPrefixes().get(6).intValue());
    }

    // 测试列属性
    @Test
    public void testPropertiesColumns() {
        anonip = new Anonip();
        assertEquals(List.of(0), anonip.getColumns());

        anonip.setColumns(List.of(5, 6));
        assertEquals(List.of(4, 5), anonip.getColumns());
    }

    private static String[] append(String[] arr, String[] elements) {
        String[] result = new String[arr.length + elements.length];
        System.arraycopy(arr, 0, result, 0, arr.length);
        System.arraycopy(elements, 0, result, arr.length, elements.length);
        return result;
    }
}
