import org.junit.Test;
import static org.junit.Assert.*;

public class TestBase62 {

    @Test
    public void testConst() {
        assertEquals(Base62.CHARSET_DEFAULT.length(), Base62.BASE);
        assertEquals(Base62.CHARSET_INVERTED.length(), Base62.BASE);
    }

    @Test
    public void testBasic() {
        assertEquals("0", Base62.encode(0));
        assertEquals(0, Base62.decode("0"));
        assertEquals(0, Base62.decode("0000"));
        assertEquals(1, Base62.decode("000001"));
        assertEquals("base62", Base62.encode(34441886726L));
        assertEquals(34441886726L, Base62.decode("base62"));
    }

    @Test
    public void testBasicInverted() {
        String charset = Base62.CHARSET_INVERTED;

        assertEquals("0", Base62.encode(0, charset));
        assertEquals(0, Base62.decode("0", charset));
        assertEquals(0, Base62.decode("0000", charset));
        assertEquals(1, Base62.decode("000001", charset));
        assertEquals("base62", Base62.encode(10231951886L, charset));
        assertEquals(10231951886L, Base62.decode("base62", charset));
    }

    @Test
    public void testBytesToInt() {
        byte[][] bytesArray = {
            {0x01},
            {0x01, 0x01},
            {(byte) 0xFF, (byte) 0xFF},
            {0x01, 0x01, 0x01},
            {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}
        };
        long[] intArray = {
            1, 0x0101, 0xFFFF, 0x010101, 0x0102030405060708L
        };

        for (int i = 0; i < bytesArray.length; i++) {
            assertEquals(intArray[i], Base62.bytesToLong(bytesArray[i]));
        }
    }

    @Test
    public void testEncodeBytes() {
        byte[][] bytesArray = {
            {0x01},
            {0x01, 0x01},
            {(byte) 0xFF, (byte) 0xFF},
            {0x01, 0x01, 0x01},
            {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}
        };
        long[] intArray = {
            1, 0x0101, 0xFFFF, 0x010101, 0x0102030405060708L
        };

        for (int i = 0; i < bytesArray.length; i++) {
            assertEquals(Base62.encode(intArray[i]), Base62.encodeBytes(bytesArray[i]));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBytesType() {
        Base62.encodeBytes("1234".getBytes());
    }

    @Test
    public void testEncodeBytesRtype() {
        String encoded = Base62.encodeBytes("1234".getBytes());
        assertTrue(encoded instanceof String);
    }

    @Test
    public void testDecodeBytes() {
        String[] strings = {"0", "1", "a", "z", "ykzvd7ga"};

        for (String s : strings) {
            assertEquals(Base62.decode(s), Base62.bytesToLong(Base62.decodeBytes(s)));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeBytesType() {
        Base62.decodeBytes(new String("1234".getBytes()));
    }    

    @Test
    public void testDecodeBytesRtype() {
        byte[] decoded = Base62.decodeBytes("1234");
        assertTrue(decoded instanceof byte[]);
    }

    @Test
    public void testRoundtrip() {
        byte[][] inputs = {
            {},
            {0x30},
            "bytes to encode".getBytes(),
            {0x01, 0x00, (byte) 0x80}
        };

        for (byte[] input : inputs) {
            String encoded = Base62.encodeBytes(input);
            byte[] output = Base62.decodeBytes(encoded);
            assertArrayEquals(input, output);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAlphabet() {
        Base62.decode("+");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidString() {
        Base62.encodeBytes(new Object().toString().getBytes());
    }

    @Test
    public void testLeadingZeros() {
        byte[][] inputs = {
            {},
            {0x00},
            {0x00, 0x00},
            {0x00, 0x01},
            new byte[61],
            new byte[62]
        };

        String[] expectedOutputs = {
            "",
            "01",
            "02",
            "011",
            "0z",
            "0z01"
        };

        for (int i = 0; i < inputs.length; i++) {
            String encoded = Base62.encodeBytes(inputs[i]);
            assertEquals(expectedOutputs[i], encoded);
            byte[] decoded = Base62.decodeBytes(encoded);
            assertArrayEquals(inputs[i], decoded);
        }
    }
}
