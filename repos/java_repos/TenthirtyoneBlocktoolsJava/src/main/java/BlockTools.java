import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.IOException;

public class BlockTools {

    public static int uint1(InputStream stream) throws IOException {
        return stream.read();
    }

    public static int uint2(InputStream stream) throws IOException {
        byte[] bytes = new byte[2];
        stream.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static int uint4(InputStream stream) throws IOException {
        byte[] bytes = new byte[4];
        stream.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static long uint8(InputStream stream) throws IOException {
        byte[] bytes = new byte[8];
        stream.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    public static byte[] hash32(InputStream stream) throws IOException {
        byte[] bytes = new byte[32];
        stream.read(bytes);
        for (int i = 0; i < bytes.length / 2; i++) {
            byte temp = bytes[i];
            bytes[i] = bytes[bytes.length - 1 - i];
            bytes[bytes.length - 1 - i] = temp;
        }
        return bytes;
    }

    public static int time(InputStream stream) throws IOException {
        return uint4(stream);
    }

    public static long varint(InputStream stream) throws IOException {
        int size = uint1(stream);
        if (size < 0xfd) {
            return size;
        } else if (size == 0xfd) {
            return uint2(stream);
        } else if (size == 0xfe) {
            return uint4(stream);
        } else {
            return uint8(stream);
        }
    }

    public static String hashStr(byte[] byteBuffer) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteBuffer) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
