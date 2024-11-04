public class NagiosHelper {
    private String performanceData = "";
    private String warningMessage = "";
    private String criticalMessage = "";
    private String unknownMessage = "";

    private static final int OK_CODE = 0;
    private static final int WARNING_CODE = 1;
    private static final int CRITICAL_CODE = 2;
    private static final int UNKNOWN_CODE = 3;

    public String getMessage() {
        String code = getStatus();
        String output = String.format("%s: Status %s. %s", code, code, getCombinedMessage().trim());
        if (!performanceData.isEmpty()) {
            output = String.format("%s: %s Status %s. %s|%s", code, performanceData, code, getCombinedMessage().trim(), performanceData);
        }
        return output.trim();
    }

    public int getCode() {
        if (!unknownMessage.isEmpty()) return UNKNOWN_CODE;
        if (!criticalMessage.isEmpty()) return CRITICAL_CODE;
        if (!warningMessage.isEmpty()) return WARNING_CODE;
        return OK_CODE;
    }

    public void appendMessage(int code, String msg) {
        if (code == WARNING_CODE) {
            warningMessage += msg;
        } else if (code == CRITICAL_CODE) {
            criticalMessage += msg;
        } else if (code == UNKNOWN_CODE) {
            unknownMessage += msg;
        }
    }

    public void appendMetrics(String performanceData, String warningMessage, String criticalMessage) {
        this.performanceData += performanceData;
        appendMessage(WARNING_CODE, warningMessage);
        appendMessage(CRITICAL_CODE, criticalMessage);
    }

    private String getStatus() {
        int code = getCode();
        switch (code) {
            case WARNING_CODE: return "WARNING";
            case CRITICAL_CODE: return "CRITICAL";
            case UNKNOWN_CODE: return "UNKNOWN";
            default: return "OK";
        }
    }

    private String getCombinedMessage() {
        return warningMessage + criticalMessage + unknownMessage;
    }
}
