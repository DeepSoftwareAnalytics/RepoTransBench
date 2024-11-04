import java.util.List;

public class Invalid extends Error {
    private List<Object> path;
    private String errorMessage;

    public Invalid(String message, List<Object> path, String errorMessage) {
        super(message);
        this.path = path;
        this.errorMessage = errorMessage != null ? errorMessage : message;
    }

    public Invalid(String message, List<Object> path) {
        this(message, path, null);
    }

    public Invalid(String message) {
        this(message, null, null);
    }

    public List<Object> getPath() {
        return path;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        String pathStr = path != null ? " @ data[" + String.join("][", path.toString()) + "]" : "";
        return super.toString() + pathStr;
    }
}
