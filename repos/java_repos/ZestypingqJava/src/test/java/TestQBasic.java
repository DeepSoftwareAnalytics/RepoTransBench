import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TestQBasic {

    @BeforeEach
    public void setUp() {
        File file = new File("/tmp/q");
        if (file.exists()) {
            file.delete();
        }
    }

    @AfterEach
    public void tearDown() {
        setUp();
    }

    private void assertInQLog(String string) throws IOException {
        File file = new File("/tmp/q");
        assertTrue(file.exists());

        FileReader reader = new FileReader(file);
        char[] chars = new char[(int) file.length()];
        reader.read(chars);
        String logdata = new String(chars);
        reader.close();

        Pattern expectedPattern = Pattern.compile(".*" + string + ".*", Pattern.DOTALL);
        if (!expectedPattern.matcher(logdata).find()) {
            fail(String.format("Regexp didn't match: %s not found in\n%s\n%s\n%s",
                    expectedPattern.pattern(), "-".repeat(75), logdata, "-".repeat(75)));
        }
    }

    @Test
    public void testQLogMessage() throws IOException {
        Q q = new Q();
        q.q("Test message");
        assertInQLog("Test message");
    }

    @Test
    public void testQFunctionCall() throws IOException {
        Q q = new Q();

        Q.QFunction test = (arg) -> "RetVal";

        assertEquals("RetVal", test.apply("ArgVal"));

        assertInQLog("ArgVal");
        assertInQLog("RetVal");
    }

    @Test
    public void testQArgumentOrderArguments() throws IOException {
        Q q = new Q();
        q.setWriterColor(false);

        class A {
            A(String two, String three, String four) {
                q.q(two, three, four);
            }
        }

        new A("ArgVal1", "ArgVal2", "ArgVal3");
        assertInQLog(".*__init__:.*two='ArgVal1'.*three='ArgVal2'.*four='ArgVal3'.*");
    }

    @Test
    public void testQArgumentOrderAttributes1() throws IOException {
        Q q = new Q();
        q.setWriterColor(false);

        class A {
            String attrib1;
            String attrib2;

            A(String two, String three, String four) {
                this.attrib1 = "Attrib1";
                this.attrib2 = "Attrib2";
                q.q(this.attrib1, this.attrib2);
            }
        }

        new A("ArgVal1", "ArgVal2", "ArgVal3");
        assertInQLog(".*__init__:.*self.attrib1='Attrib1',.*self.attrib2='Attrib2'.*");
    }

    @Test
    public void testQArgumentOrderAttributes2() throws IOException {
        Q q = new Q();
        q.setWriterColor(false);

        class A {
            String attrib1;
            String attrib2;

            A(String two, String three, String four) {
                this.attrib1 = "Attrib1";
                this.attrib2 = "Attrib2";
                q.q(this.attrib1, this.attrib2);
            }
        }

        new A("ArgVal1", "ArgVal2", "ArgVal3");
        assertInQLog(".*__init__:.*this.attrib1='Attrib1',.*this.attrib2='Attrib2'.*");
    }

    @Test
    public void testQMultipleCallsOnLine() throws IOException {
        Q q = new Q();
        q.setWriterColor(false);

        class A {
            String attrib1;
            String attrib2;

            A(String two, String three, String four) {
                this.attrib1 = "Attrib1";
                this.attrib2 = "Attrib2";
                String result1 = qLogAndReturn(two, this.attrib1, q);
                String result2 = qLogAndReturn(three, this.attrib2, q);
                q.q(result1 + result2, four);
            }
        }

        new A("ArgVal1", "ArgVal2", "ArgVal3");
        assertInQLog(".*__init__:.*two='ArgVal1',.*self.attrib1='Attrib1'.*__init__:.*three='ArgVal2',.*self.attrib2='Attrib2'.*'ArgVal1ArgVal2',.*four='ArgVal3'.*");
    }

    private String qLogAndReturn(String arg1, String arg2, Q q) {
        q.q(arg1, arg2);
        return arg1 + arg2;
    }

    @Test
    public void testQArgumentOrderAttributesAndArguments() throws IOException {
        Q q = new Q();
        q.setWriterColor(false);

        class A {
            String attrib1;
            String attrib2;

            A(String two, String three, String four) {
                this.attrib1 = "Attrib1";
                this.attrib2 = "Attrib2";
                q.q(two, three, this.attrib1, four, this.attrib2);
            }
        }

        new A("ArgVal1", "ArgVal2", "ArgVal3");
        assertInQLog(".*__init__:.*two='ArgVal1'.*three='ArgVal2'.*self.attrib1='Attrib1'.*four='ArgVal3'.*self.attrib2='Attrib2'.*");
    }

    @Test
    public void testQTrace() throws IOException {
        Q q = new Q();
        q.setWriterColor(false);

        Q.QFunction log1 = (msg) -> {
            return msg != null ? msg : "default";
        };

        Q.QFunction log2 = (msg) -> {
            return msg != null ? msg : "default";
        };

        log1.apply("log1 message");
        log2.apply("log2 message");

        assertInQLog("log1\\('log1 message'\\)");
        assertInQLog("log2\\('log2 message'\\)");
    }

    @Test
    public void testQNestedBadWrapper() throws IOException {
        Q q = new Q();
        q.setWriterColor(false);

        java.util.function.Function<Q.QFunction, Q.QFunction> wrapper = (func) -> {
            return (args) -> {
                return func.apply(args);
            };
        };

        Q.QFunction decoratedLogBad = wrapper.apply(
                q.apply(
                        wrapper.apply(
                                (msg) -> {
                                    return msg != null ? msg : "default";
                                }
                        )
                )
        );

        decoratedLogBad.apply("decorated bad message");
        assertInQLog("do_nothing\\((?:\\n\\s*)?'decorated bad message'\\)");
        assertInQLog("-> 'decorated bad message'");
    }

    @Test
    public void testQNestedGoodWrappers() throws IOException {
        Q q = new Q();
        q.setWriterColor(false);

        java.util.function.Function<Q.QFunction, Q.QFunction> wrapper = (func) -> {
            return (args) -> {
                return func.apply(args);
            };
        };

        Q.QFunction decoratedLogGood = wrapper.apply(
                q.apply(
                        wrapper.apply(
                                (msg) -> {
                                    return msg != null ? msg : "default";
                                }
                        )
                )
        );

        decoratedLogGood.apply("decorated good message");
        assertInQLog("decoratedLogGood\\((?:\\n\\s*)?'decorated good message'\\)");
        assertInQLog("-> 'decorated good message'");
    }

    @Test
    public void testQTraceMethod() throws IOException {
        Q q = new Q();
        q.setWriterColor(false);

        A a = new A();
        a.run1("first message");
        A.run2("second message");
        A.run3("third message");

        assertInQLog(".*\\bA.run1\\(.*'first message'\\).*");
        assertInQLog("-> 'first message'");

        assertInQLog(".*\\bA.run2\\(.*'second message'\\).*");
        assertInQLog("-> 'second message'");

        assertInQLog(".*\\bA.run3\\(.*'third message'\\).*");
        assertInQLog("-> 'third message'");
    }
}
