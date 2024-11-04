import java.util.HashMap;
import java.util.Map;

public class Reporter {

    private static final Map<String, Class<? extends Reporter>> TYPES = new HashMap<>();

    static {
        TYPES.put("json", JSONReporter.class);
    }

    public static Reporter fromIdentifier(String identifier) {
        Class<? extends Reporter> clazz = TYPES.get(identifier);
        if (clazz != null) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void report(String[] allFiles, String[] modifiedFiles) {
        // Default implementation does nothing
    }
}

class JSONReporter extends Reporter {
    @Override
    public void report(String[] allFiles, String[] modifiedFiles) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        String json = gson.toJson(new JsonReport(modifiedFiles.length == 0 ? "clean" : "dirty", modifiedFiles));
        System.out.println(json);
    }

    public static class JsonReport {
        private String status;
        private String[] files;

        public JsonReport(String status, String[] files) {
            this.status = status;
            this.files = files;
        }

        public String getStatus() {
            return status;
        }

        public String[] getFiles() {
            return files;
        }
    }
}
