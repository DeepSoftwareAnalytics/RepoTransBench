import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Filtering {

    public static final String DOWNLOADED_IDS_FILE_NAME = "downloaded_ids.json";
    public static final String KEY_IDS = "ids";

    public static List<String> readExclude(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("File not found or not a file: " + filePath);
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            List<String> ids = new ArrayList<>();
            jsonObject.getAsJsonArray(KEY_IDS).forEach(id -> ids.add(id.getAsString()));
            return ids;
        } catch (IOException e) {
            System.out.println("Error reading file: " + filePath);
            return null;
        }
    }

    public static void updateDownloadStats(String activityId, String directory) {
        File file = new File(directory, DOWNLOADED_IDS_FILE_NAME);
        JsonObject jsonObject;

        if (!file.exists()) {
            jsonObject = new JsonObject();
            jsonObject.add(KEY_IDS, new Gson().toJsonTree(new ArrayList<String>()));
        } else {
            try (FileReader reader = new FileReader(file)) {
                jsonObject = new Gson().fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                jsonObject = new JsonObject();
                jsonObject.add(KEY_IDS, new Gson().toJsonTree(new ArrayList<String>()));
            }
        }

        List<String> ids = new ArrayList<>();
        jsonObject.getAsJsonArray(KEY_IDS).forEach(id -> ids.add(id.getAsString()));

        if (!ids.contains(activityId)) {
            ids.add(activityId);
            ids.sort(String::compareTo);
            jsonObject.add(KEY_IDS, new Gson().toJsonTree(ids));

            try (FileWriter writer = new FileWriter(file)) {
                new Gson().toJson(jsonObject, writer);
            } catch (IOException e) {
                System.out.println("Error writing file: " + file.getPath());
            }
        }
    }
}
