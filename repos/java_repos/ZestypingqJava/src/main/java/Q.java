import java.io.*;
import java.text.DecimalFormat;
import java.util.function.Function;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Q {
    private static final String OUTPUT_PATH = System.getProperty("java.io.tmpdir") + "/q";

    private static final String NORMAL = "\u001b[0m";
    private static final String RED = "\u001b[31m";
    private static final String GREEN = "\u001b[32m";
    private static final String YELLOW = "\u001b[33m";
    private static final String BLUE = "\u001b[34m";
    private static final String MAGENTA = "\u001b[35m";
    private static final String CYAN = "\u001b[36m";

    private final Writer writer;
    private int indent = 0;
    private boolean inConsole = false;

    public Q() {
        // Fix initializing WriterUtil instead of FileWriter directly
        this.writer = new Writer(new WriterUtil(OUTPUT_PATH), System.nanoTime());
    }

    public void q(Object... args) {
        StackTraceElement caller = new Throwable().getStackTrace()[1];
        String funcName = caller.getClassName() + "." + caller.getMethodName();
        StringBuffer sb = new StringBuffer();
        sb.append(funcName);
        sb.append(": ");
        for (Object arg : args) {
            sb.append(arg).append(",");
        }
        // Remove the trailing comma
        if (sb.length() > 0) sb.setLength(sb.length() - 1);

        writer.write(sb.toString(), args);
    }

    public QFunction apply(QFunction func) {
        return (args) -> {
            String result = func.apply(args);
            q(args, result);
            return result;
        };
    }

    public void setWriterColor(boolean color) {
        this.writer.setColor(color);
    }

    // Nested classes
    public static class Writer {
        private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");
        private final WriterUtil fileWriter; // Fix type to WriterUtil
        private final long startTime;
        private boolean color = true;
        private long lastWrite;

        public Writer(WriterUtil fileWriter, long startTime) {
            this.fileWriter = fileWriter;
            this.startTime = startTime;
        }

        public void setColor(boolean color) {
            this.color = color;
        }

        public void write(String funcName, Object... args) {
            StringBuilder sb = new StringBuilder();
            sb.append(formatElapsedTime()).append(" ");
            if (color) {
                sb.append(YELLOW);
            }
            sb.append(funcName).append(": ");
            if (color) {
                sb.append(NORMAL);
            }

            String sep = "";
            for (Object arg : args) {
                sb.append(sep).append(format(arg));
                sep = ", ";
            }

            fileWriter.write(sb.toString());
        }

        private String formatElapsedTime() {
            double elapsed = (System.nanoTime() - startTime) / 1e9;
            return DECIMAL_FORMAT.format(elapsed);
        }

        private String format(Object object) {
            if (object instanceof String) {
                return "\"" + object + "\"";
            }
            return object.toString();
        }
    }

    public static class WriterUtil {
        private final String path;

        public WriterUtil(String path) {
            this.path = path;
        }

        public void write(String content) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true), UTF_8))) {
                writer.write(content + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FunctionalInterface
    public interface QFunction {
        String apply(String args);
    }
}
