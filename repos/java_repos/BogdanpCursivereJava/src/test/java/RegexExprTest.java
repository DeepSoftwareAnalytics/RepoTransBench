import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegexExprTest {

    @ParameterizedTest
    @MethodSource("provideTestExprs")
    public void testExprs(RegexExpr expr, String expected) {
        assertEquals(expected, expr.toString());
    }

    private static Stream<Arguments> provideTestExprs() {
        return Stream.of(
            Arguments.of(RegexExpr.beginningOfLine(), "^"),
            Arguments.of(RegexExpr.endOfLine(), "$"),
            Arguments.of(RegexExpr.anything(), "."),

            Arguments.of(RegexExpr.literal("a"), "a"),
            Arguments.of(RegexExpr.literal("["), "["),

            Arguments.of(RegexExpr.text("a"), "a"),
            Arguments.of(RegexExpr.text("["), "\\["),

            Arguments.of(RegexExpr.anyOf("abc"), "[abc]"),
            Arguments.of(RegexExpr.anyOf(RegexExpr.text("abc").toString()), "[abc]"),
            Arguments.of(RegexExpr.anyOf(RegexExpr.text("ab[").toString()), "[ab\\[]"),
            Arguments.of(RegexExpr.anyOf(RegexExpr.text("a-z").toString()), "[a\\-z]"),
            Arguments.of(RegexExpr.anyOf(RegexExpr.inRange("a", "z").toString()), "[a-z]"),

            Arguments.of(RegexExpr.noneOf("abc"), "[^abc]"),
            Arguments.of(RegexExpr.noneOf(RegexExpr.text("abc").toString()), "[^abc]"),
            Arguments.of(RegexExpr.noneOf(RegexExpr.text("ab[").toString()), "[^ab\\[]"),
            Arguments.of(RegexExpr.noneOf(RegexExpr.literal("ab[").toString()), "[^ab[]"),
            Arguments.of(RegexExpr.noneOf(RegexExpr.text("a-z").toString()), "[^a\\-z]"),
            Arguments.of(RegexExpr.noneOf(RegexExpr.inRange("a", "z").toString()), "[^a-z]"),

            Arguments.of(RegexExpr.inRange("a", "z"), "a-z"),

            Arguments.of(RegexExpr.zeroOrMore(RegexExpr.text("abc")), "(?:abc)*"),
            Arguments.of(RegexExpr.zeroOrMore(RegexExpr.anyOf("abc")), "[abc]*"),

            Arguments.of(RegexExpr.oneOrMore(RegexExpr.text("abc")), "(?:abc)+"),
            Arguments.of(RegexExpr.oneOrMore(RegexExpr.anyOf("abc")), "[abc]+"),

            Arguments.of(RegexExpr.maybe(RegexExpr.text("a")), "(?:a)?"),
            Arguments.of(RegexExpr.maybe(RegexExpr.text("a").seq(RegexExpr.text("b"))), "(?:ab)?"),
            Arguments.of(RegexExpr.maybe(RegexExpr.noneOf(RegexExpr.inRange("a", "z").toString())), "[^a-z]?"),
            Arguments.of(RegexExpr.maybe(RegexExpr.text("a").seq(RegexExpr.anyOf(RegexExpr.inRange("a", "z").toString()))), "(?:a[a-z])?"),

            Arguments.of(RegexExpr.repeated(RegexExpr.text("a"), 5), "(?:a){5}"),
            Arguments.of(RegexExpr.repeated(RegexExpr.text("a"), 2).toString(), "(?:a){2,}"),
            Arguments.of(RegexExpr.repeated(RegexExpr.text("a"), 2, 0).toString(), "(?:a){0,2}"),
            Arguments.of(RegexExpr.repeated(RegexExpr.text("a"), 1, 2).toString(), "(?:a){1,2}"),
            Arguments.of(RegexExpr.repeated(RegexExpr.text("a"), 1, 2, false).toString(), "(?:a){1,2}?"),

            Arguments.of(RegexExpr.alternative(RegexExpr.text("a"), RegexExpr.text("b")), "(?:a)|(?:b)"),
            Arguments.of(RegexExpr.alternative(RegexExpr.anyOf("abc"), RegexExpr.text("d")), "[abc]|(?:d)"),
            Arguments.of(RegexExpr.alternative(RegexExpr.text("a"), RegexExpr.alternative(RegexExpr.text("b"), RegexExpr.text("c"))), "(?:a)|(?:b)|(?:c)"),
            Arguments.of(RegexExpr.alternative(RegexExpr.text("a"), RegexExpr.text("b"), RegexExpr.text("c")), "(?:a)|(?:b)|(?:c)"),
            Arguments.of(RegexExpr.alternative(RegexExpr.literal("[]"), RegexExpr.text("b")), "(?:[])|(?:b)"),

            Arguments.of(RegexExpr.group(RegexExpr.text("a")), "(a)"),
            Arguments.of(RegexExpr.group(RegexExpr.text("a"), "foo", true), "(?P<foo>a)"),
            Arguments.of(RegexExpr.group(RegexExpr.text("a"), null, false), "(?:a)")
        );
    }
}
