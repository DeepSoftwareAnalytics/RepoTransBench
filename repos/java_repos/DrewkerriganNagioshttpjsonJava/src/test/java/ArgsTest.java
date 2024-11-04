// src/test/java/com/example/ArgsTest.java

package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArgsTest {

    /**
     * Tests for argparse (argument parsing)
     */

    @Test
    void testParserDefaults() {
        Args parser = Args.parseArgs(new String[]{"-H", "foobar"});
        assertFalse(parser.isDebug());
        assertFalse(parser.isSsl());
        assertFalse(parser.isInsecure());
    }

    @Test
    void testParserWithDebug() {
        Args parser = Args.parseArgs(new String[]{"-H", "foobar", "-d"});
        assertTrue(parser.isDebug());
    }

    @Test
    void testParserWithPort() {
        Args parser = Args.parseArgs(new String[]{"-H", "foobar", "-P", "8888"});
        assertEquals("8888", parser.getPort());
    }

    @Test
    void testParserWithSeparator() {
        Args parser = Args.parseArgs(new String[]{"-H", "foobar", "-f", "_", "-F", "_"});
        assertEquals("_", parser.getSeparator());
        assertEquals("_", parser.getValueSeparator());
    }
}
