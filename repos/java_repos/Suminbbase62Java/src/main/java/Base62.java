public class Base62 {
    public static final int BASE = 62;
    public static final String CHARSET_DEFAULT = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String CHARSET_INVERTED = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encode(long n) {
        return encode(n, CHARSET_DEFAULT);
    }

    public static String encode(long n, String charset) {
        StringBuilder chs = new StringBuilder();
        while (n > 0) {
            int r = (int) (n % BASE);
            n = n / BASE;
            chs.insert(0, charset.charAt(r));
        }
        if (chs.length() == 0) {
            return "0";
        }
        return chs.toString();
    }

    public static String encodeBytes(byte[] barray) {
        return encodeBytes(barray, CHARSET_DEFAULT);
    }

    public static String encodeBytes(byte[] barray, String charset) {
        _checkType(barray, byte[].class);

        // Count the number of leading zeros.
        int leadingZerosCount = 0;
        for (int i = 0; i < barray.length; i++) {
            if (barray[i] != 0) {
                break;
            }
            leadingZerosCount++;
        }

        // Encode the leading zeros as "0" followed by a character indicating the count.
        int n = leadingZerosCount / (charset.length() - 1);
        int r = leadingZerosCount % (charset.length() - 1);
        StringBuilder zeroPadding = new StringBuilder();
        for (int i = 0; i < n; i++) {
            zeroPadding.append("0").append(charset.charAt(charset.length() - 1));
        }
        if (r != 0) {
            zeroPadding.append("0").append(charset.charAt(r));
        }

        // Special case: input is empty or is entirely null bytes.
        if (leadingZerosCount == barray.length) {
            return zeroPadding.toString();
        }

        String value = encode(bytesToLong(barray), charset);
        return zeroPadding + value;
    }

    public static long decode(String encoded) {
        return decode(encoded, CHARSET_DEFAULT);
    }

    public static long decode(String encoded, String charset) {
        _checkType(encoded, String.class);

        int l = encoded.length();
        long v = 0;
        for (int i = 0; i < l; i++) {
            char x = encoded.charAt(i);
            v += _value(x, charset) * Math.pow(BASE, l - (i + 1));
        }
        return v;
    }

    public static byte[] decodeBytes(String encoded) {
        return decodeBytes(encoded, CHARSET_DEFAULT);
    }

    public static byte[] decodeBytes(String encoded, String charset) {
        StringBuilder encodedSb = new StringBuilder(encoded);

        byte[] leadingNullBytes = new byte[0];
        while (encodedSb.length() >= 2 && encodedSb.substring(0, 1).equals("0")) {
            int count = _value(encodedSb.charAt(1), charset);
            byte[] temp = leadingNullBytes;
            leadingNullBytes = new byte[temp.length + count];
            System.arraycopy(temp, 0, leadingNullBytes, 0, temp.length);
            encodedSb.delete(0, 2);
        }

        long decoded = decode(encodedSb.toString(), charset);
        return longToBytes(decoded, leadingNullBytes);
    }

    private static int _value(char ch, String charset) {
        int index = charset.indexOf(ch);
        if (index == -1) {
            throw new IllegalArgumentException("base62: Invalid character (" + ch + ")");
        }
        return index;
    }

    private static void _checkType(Object value, Class<?> expectedType) {
        if (!expectedType.isInstance(value)) {
            throw new IllegalArgumentException(
                "Expected " + expectedType.getSimpleName() + " object, not " + value.getClass().getSimpleName()
            );
        }
    }

    public static byte[] longToBytes(long x, byte[] base) {
        byte[] res = base;
        while (x != 0) {
            byte[] temp = res;
            res = new byte[temp.length + 1];
            res[0] = (byte) (x & 0xFF);
            x >>= 8;
            System.arraycopy(temp, 0, res, 1, temp.length);
        }
        return res;
    }

    public static long bytesToLong(byte[] bytes) {
        long res = 0;
        for (byte b : bytes) {
            res = (res << 8) | (b & 0xFF);
        }
        return res;
    }
}
