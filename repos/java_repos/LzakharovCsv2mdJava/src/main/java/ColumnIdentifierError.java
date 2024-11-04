public class ColumnIdentifierError extends Exception {
    public ColumnIdentifierError(String column) {
        super("Invalid column identifier \"" + column + "\". Must be non-negative integer or range of non-negative integers separated by \"-\".");
    }
}
