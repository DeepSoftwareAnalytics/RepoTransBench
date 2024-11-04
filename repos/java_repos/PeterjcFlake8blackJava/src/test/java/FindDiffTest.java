package test.java;

import main.java.FindDiff;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class FindDiffTest {

    @Test
    public void testNoDifference() {
        assertArrayEquals(new int[]{1, 0}, FindDiff.findDiffStart("hello", "hello"));
    }

    @Test
    public void testDifferenceInMiddle() {
        assertArrayEquals(new int[]{0, 2}, FindDiff.findDiffStart("hello", "heLlo"));
    }

    @Test
    public void testDifferenceAtEnd() {
        assertArrayEquals(new int[]{0, 4}, FindDiff.findDiffStart("hello", "hell"));
    }

    @Test
    public void testDifferenceInLongerOld() {
        assertArrayEquals(new int[]{0, 5}, FindDiff.findDiffStart("hello world", "hello"));
    }

    @Test
    public void testDifferenceInLongerNew() {
        assertArrayEquals(new int[]{0, 5}, FindDiff.findDiffStart("hello", "hello world"));
    }

    @Test
    public void testDifferenceWithEmptyOld() {
        assertArrayEquals(new int[]{0, 0}, FindDiff.findDiffStart("", "hello"));
    }

    @Test
    public void testDifferenceWithEmptyNew() {
        assertArrayEquals(new int[]{0, 0}, FindDiff.findDiffStart("hello", ""));
    }

    @Test
    public void testDifferenceWithPartialMatch() {
        assertArrayEquals(new int[]{0, 4}, FindDiff.findDiffStart("hella", "hello"));
    }

    @Test
    public void testDifferenceWithExtraChars() {
        assertArrayEquals(new int[]{0, 5}, FindDiff.findDiffStart("hello!!", "hello"));
    }

    @Test
    public void testDifferenceWithCaseSensitive() {
        assertArrayEquals(new int[]{0, 0}, FindDiff.findDiffStart("Hello", "hello"));
    }
}
