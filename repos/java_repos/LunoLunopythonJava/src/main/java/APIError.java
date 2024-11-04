public class APIError extends Exception {
    private final String code;
    private final String message;

    public APIError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
