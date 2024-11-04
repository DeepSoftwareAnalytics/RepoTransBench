import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class IdbLib {

    public static class FileSection {
        private InputStream fh;
        private long start;
        private long end;
        private long curpos;

        public FileSection(InputStream fh, long start, long end) {
            this.fh = fh;
            this.start = start;
            this.end = end;
            this.curpos = 0;
            try {
                this.fh.skip(this.start);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public byte[] read(int size) throws IOException {
            long want = this.end - this.start - this.curpos;
            if (size > 0 && want > size) {
                want = size;
            }

            if (want <= 0) {
                return new byte[0];
            }

            this.fh.skip(this.curpos + this.start);
            byte[] data = new byte[(int) want];
            int bytesRead = this.fh.read(data);
            this.curpos += bytesRead;
            return Arrays.copyOf(data, bytesRead);
        }

        public void seek(long offset, int whence) throws IOException {
            long newpos;
            if (whence == 0) {
                newpos = offset;
            } else if (whence == 1) {
                newpos = this.curpos + offset;
            } else if (whence == 2) {
                newpos = this.end - this.start + offset;
            } else {
                throw new IllegalArgumentException("Invalid whence value");
            }

            if (newpos < 0 || newpos > this.end - this.start) {
                throw new IOException("Illegal offset");
            }

            this.curpos = newpos;
            this.fh.skip(this.curpos + this.start);
        }

        public long tell() {
            return this.curpos;
        }
    }

    public static class Object {
        public int key;

        public Object(int num) {
            this.key = num;
        }

        @Override
        public String toString() {
            return "o(" + this.key + ")";
        }
    }

    public static int binarySearch(Object[] a, int k) {
        int first = 0, last = a.length;
        while (first < last) {
            int mid = (first + last) >> 1;
            if (k < a[mid].key) {
                last = mid;
            } else {
                first = mid + 1;
            }
        }
        return first - 1;
    }

    public static InputStream makeStringIO(byte[] data) {
        return new ByteArrayInputStream(data);
    }
}
