import java.util.Objects;
import java.io.File; // Added this import statement

public class Position {
    private String filename;
    private String func;
    private int lineNo;
    private Integer lineOrder;

    public Position(String filename, String func, int lineNo) {
        this.filename = filename;
        this.func = func;
        this.lineNo = lineNo;
        this.lineOrder = null;
    }

    public int hashCode() {
        return Objects.hash(filename, func, lineNo);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return lineNo == position.lineNo &&
                Objects.equals(filename, position.filename) &&
                Objects.equals(func, position.func);
    }

    public String toString() {
        String lineNoStr = (lineNo > 0 ? "," + lineNo : "");
        String lineOrderStr = (lineOrder != null ? "@" + lineOrder : "");
        return "(" + new File(filename).getName() + "," + func + lineNoStr + lineOrderStr + ")";
    }

    public String xmlStr() {
        return func;
    }

    // Getters and setters
}

