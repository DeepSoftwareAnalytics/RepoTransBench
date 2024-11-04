public class SchemaError extends Error {
    public SchemaError(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
