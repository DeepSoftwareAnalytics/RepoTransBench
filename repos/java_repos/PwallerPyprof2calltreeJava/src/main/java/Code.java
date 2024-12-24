public class Code {
    public final String filename;
    public final int firstlineno;
    public final String name;

    public Code(String filename, int firstlineno, String name) {
        this.filename = filename;
        this.firstlineno = firstlineno;
        this.name = name;
    }

    @Override
    public String toString() {
        return "<Code: " + filename + ", " + firstlineno + ", " + name + ">";
    }
}
