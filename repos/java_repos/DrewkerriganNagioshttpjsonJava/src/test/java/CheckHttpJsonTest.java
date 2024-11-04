import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class RulesHelper {
    String separator = ".";
    String valueSeparator = ":";
    boolean debug = false;
    List<String> keyThresholdWarning;
    List<String> keyValueList;
    List<String> keyValueListNot;
    List<String> keyList;
    List<String> keyThresholdCritical;
    List<String> keyValueListCritical;
    List<String> keyValueListNotCritical;
    List<String> keyValueListUnknown;
    List<String> keyListCritical;
    List<String> metricList;

    public RulesHelper dashM(List<String> data) {
        this.metricList = data;
        return this;
    }

    public RulesHelper dashE(List<String> data) {
        this.keyList = data;
        return this;
    }

    public RulesHelper dashCapitalE(List<String> data) {
        this.keyListCritical = data;
        return this;
    }

    public RulesHelper dashQ(List<String> data) {
        this.keyValueList = data;
        return this;
    }

    public RulesHelper dashCapitalQ(List<String> data) {
        this.keyValueListCritical = data;
        return this;
    }

    public RulesHelper dashY(List<String> data) {
        this.keyValueListNot = data;
        return this;
    }

    public RulesHelper dashCapitalY(List<String> data) {
        this.keyValueListNotCritical = data;
        return this;
    }

    public RulesHelper dashU(List<String> data) {
        this.keyValueListUnknown = data;
        return this;
    }

    public RulesHelper dashW(List<String> data) {
        this.keyThresholdWarning = data;
        return this;
    }

    public RulesHelper dashC(List<String> data) {
        this.keyThresholdCritical = data;
        return this;
    }
}

class UtilTest {

    private final int OK_CODE = 0;
    private final int WARNING_CODE = 1;
    private final int CRITICAL_CODE = 2;
    private final int UNKNOWN_CODE = 3;

    private RulesHelper rules;

    @BeforeEach
    void setUp() {
        rules = new RulesHelper();
    }

    private void checkData(RulesHelper args, String jsonData, int expectedCode) {
        JsonRuleProcessor processor = new JsonRuleProcessor(jsonData, args);
        NagiosHelper nagios = new NagiosHelper();
        nagios.appendMessage(WARNING_CODE, processor.checkWarning());
        nagios.appendMessage(CRITICAL_CODE, processor.checkCritical());
        nagios.appendMetrics(processor.checkMetrics());
        nagios.appendMessage(UNKNOWN_CODE, processor.checkUnknown());
        assertEquals(expectedCode, nagios.getCode());
    }

    @Test
    void testMetrics() {
        checkData(rules.dashM(List.of("metric,,1:4,1:5")), "{\"metric\": 5}", WARNING_CODE);
        checkData(rules.dashM(List.of("metric,,1:5,1:4")), "{\"metric\": 5}", CRITICAL_CODE);
        checkData(rules.dashM(List.of("metric,,1:5,1:5,6,10")), "{\"metric\": 5}", CRITICAL_CODE);
        checkData(rules.dashM(List.of("metric,,1:5,1:5,1,4")), "{\"metric\": 5}", CRITICAL_CODE);
        checkData(rules.dashM(List.of("metric,s,@1:4,@6:10,1,10")), "{\"metric\": 5}", OK_CODE);
        checkData(rules.dashM(List.of("(*).value,s,1:5,1:5")), "[{\"value\": 5},{\"value\": 100}]", CRITICAL_CODE);
        checkData(rules.dashM(List.of("metric>foobar,,1:4,1:5")), "{\"metric\": 5}", WARNING_CODE);
    }

    @Test
    void testUnknown() {
        checkData(rules.dashU(List.of("metric,0")), "{\"metric\": 3}", UNKNOWN_CODE);
    }

    @Test
    void testArray() {
        checkData(rules.dashQ(List.of("foo(0),bar")), "{\"foo\": [\"bar\"]}", OK_CODE);
        checkData(rules.dashQ(List.of("foo(0),foo")), "{\"foo\": [\"bar\"]}", WARNING_CODE);
        checkData(rules.dashCapitalQ(List.of("foo(1),bar")), "{\"foo\": [\"bar\"]}", CRITICAL_CODE);
    }

    @Test
    void testExists() {
        checkData(rules.dashE(List.of("nothere")), "{\"metric\": 5}", WARNING_CODE);
        checkData(rules.dashCapitalE(List.of("nothere")), "{\"metric\": 5}", CRITICAL_CODE);
        checkData(rules.dashE(List.of("metric")), "{\"metric\": 5}", OK_CODE);
    }

    @Test
    void testEquality() {
        checkData(rules.dashQ(List.of("metric,6")), "{\"metric\": 5}", WARNING_CODE);
        checkData(rules.dashCapitalQ(List.of("metric,6")), "{\"metric\": 5}", CRITICAL_CODE);
        checkData(rules.dashQ(List.of("metric,5")), "{\"metric\": 5}", OK_CODE);
    }

    @Test
    void testEqualityColon() {
        rules.valueSeparator = "_";
        checkData(rules.dashQ(List.of("metric,foo:bar")), "{\"metric\": \"foo:bar\"}", OK_CODE);
    }

    @Test
    void testNonEquality() {
        checkData(rules.dashY(List.of("metric,6")), "{\"metric\": 6}", WARNING_CODE);
        checkData(rules.dashCapitalY(List.of("metric,6")), "{\"metric\": 6}", CRITICAL_CODE);
        checkData(rules.dashY(List.of("metric,5")), "{\"metric\": 6}", OK_CODE);
    }

    @Test
    void testWarningThresholds() {
        checkData(rules.dashW(List.of("metric,5")), "{\"metric\": 5}", OK_CODE);
        checkData(rules.dashW(List.of("metric,5:")), "{\"metric\": 5}", OK_CODE);
        checkData(rules.dashW(List.of("metric,~:5")), "{\"metric\": 5}", OK_CODE);
        checkData(rules.dashW(List.of("metric,1:5")), "{\"metric\": 5}", OK_CODE);
        checkData(rules.dashW(List.of("metric,@5")), "{\"metric\": 6}", OK_CODE);
        checkData(rules.dashW(List.of("metric,@5:")), "{\"metric\": 4}", OK_CODE);
        checkData(rules.dashW(List.of("metric,@~:5")), "{\"metric\": 6}", OK_CODE);
        checkData(rules.dashW(List.of("metric,@1:5")), "{\"metric\": 6}", OK_CODE);
        checkData(rules.dashW(List.of("metric,5")), "{\"metric\": 6}", WARNING_CODE);
        checkData(rules.dashW(List.of("metric,5:")), "{\"metric\": 4}", WARNING_CODE);
        checkData(rules.dashW(List.of("metric,~:5")), "{\"metric\": 6}", WARNING_CODE);
        checkData(rules.dashW(List.of("metric,1:5")), "{\"metric\": 6}", WARNING_CODE);
        checkData(rules.dashW(List.of("metric,@5")), "{\"metric\": 5}", WARNING_CODE);
        checkData(rules.dashW(List.of("metric,@5:")), "{\"metric\": 5}", WARNING_CODE);
        checkData(rules.dashW(List.of("metric,@~:5")), "{\"metric\": 5}", WARNING_CODE);
        checkData(rules.dashW(List.of("(*).value,@1:5")), "[{\"value\": 5},{\"value\": 1000}]", WARNING_CODE);
    }

    @Test
    void testCriticalThresholds() {
        checkData(rules.dashC(List.of("metric,5")), "{\"metric\": 5}", OK_CODE);
        checkData(rules.dashC(List.of("metric,5:")), "{\"metric\": 5}", OK_CODE);
        checkData(rules.dashC(List.of("metric,~:5")), "{\"metric\": 5}", OK_CODE);
        checkData(rules.dashC(List.of("metric,1:5")), "{\"metric\": 5}", OK_CODE);
        checkData(rules.dashC(List.of("metric,@5")), "{\"metric\": 6}", OK_CODE);
        checkData(rules.dashC(List.of("metric,@5:")), "{\"metric\": 4}", OK_CODE);
        checkData(rules.dashC(List.of("metric,@~:5")), "{\"metric\": 6}", OK_CODE);
        checkData(rules.dashC(List.of("metric,@1:5")), "{\"metric\": 6}", OK_CODE);
        checkData(rules.dashC(List.of("metric,5")), "{\"metric\": 6}", CRITICAL_CODE);
        checkData(rules.dashC(List.of("metric,5:")), "{\"metric\": 4}", CRITICAL_CODE);
        checkData(rules.dashC(List.of("metric,~:5")), "{\"metric\": 6}", CRITICAL_CODE);
        checkData(rules.dashC(List.of("metric,1:5")), "{\"metric\": 6}", CRITICAL_CODE);
        checkData(rules.dashC(List.of("metric,@5")), "{\"metric\": 5}", CRITICAL_CODE);
        checkData(rules.dashC(List.of("metric,@5:")), "{\"metric\": 5}", CRITICAL_CODE);
        checkData(rules.dashC(List.of("metric,@~:5")), "{\"metric\": 5}", CRITICAL_CODE);
        checkData(rules.dashC(List.of("(*).value,@1:5")), "[{\"value\": 5},{\"value\": 1000}]", CRITICAL_CODE);
    }

    @Test
    void testSeparator() {
        rules.separator = "_";
        checkData(
            rules.dashQ(List.of("(0)_gauges_jvm.buffers.direct.capacity(1)_value,1234")),
            "[{ \"gauges\": { \"jvm.buffers.direct.capacity\": [{\"value\": 215415},{\"value\": 1234}]}}]",
            OK_CODE
        );
        checkData(
            rules.dashQ(List.of("(*)_gauges_jvm.buffers.direct.capacity(1)_value,1234")),
            "[{ \"gauges\": { \"jvm.buffers.direct.capacity\": [{\"value\": 215415},{\"value\": 1234}]}}, { \"gauges\": { \"jvm.buffers.direct.capacity\": [{\"value\": 215415},{\"value\": 1235}]}}]",
            WARNING_CODE
        );
    }

    @Test
    void testArrayWithMissingElement() {
        checkData(rules.dashQ(List.of("(0).Node,there")), "[{\"Node\": \"there\"}]", OK_CODE);
        checkData(rules.dashQ(List.of("(0).Node,othervalue")), "[{\"Node\": \"othervalue\"}]", WARNING_CODE);
        checkData(rules.dashQ(List.of("(0).Node,foobar", "(1).Node,missing")), "[{\"Node\": \"foobar\"}]", WARNING_CODE);
        checkData(rules.dashQ(List.of("(0).Node,foobar", "(1).Node,missing", "(2).Node,alsomissing")), "[{\"Node\": \"foobar\"}]", WARNING_CODE);
        checkData(rules.dashQ(List.of("(0).Node,foobar", "(1).Node,missing")), "{}", CRITICAL_CODE);
    }

    @Test
    void testSubelem() {
        String data = "{\"foo\": {\"foo\": {\"foo\": \"bar\"}}}";
        checkData(rules.dashCapitalE(List.of("foo.foo.foo.foo.foo")), data, CRITICAL_CODE);
    }

    @Test
    void testSubarrayelemMissingElem() {
        String data = "[{\"capacity\": {\"value\": 1000}},{\"capacity\": {\"value\": 2200}}]";
        checkData(rules.dashCapitalE(List.of("(*).capacity.value")), data, OK_CODE);
        checkData(rules.dashCapitalE(List.of("(*).capacity.value.too_deep")), data, CRITICAL_CODE);
        checkData(rules.dashCapitalE(List.of("foo")), data, CRITICAL_CODE);
    }

    @Test
    void testEmptyKeyValueArray() {
        String data = "[{\"update_status\": \"finished\"},{\"update_status\": \"finished\"}]";
        checkData(rules.dashQ(List.of("(*).update_status,finished")), data, OK_CODE);

        data = "[{\"update_status\": \"finished\"},{\"update_status\": \"failure\"}]";
        checkData(rules.dashQ(List.of("(*).update_status,finished")), data, WARNING_CODE);

        data = "[]";
        checkData(rules.dashQ(List.of("(*).update_status,warn_me")), data, CRITICAL_CODE);
    }
}
