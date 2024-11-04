import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SteamGames extends SteamAPI {

    private int num;
    private Map<Integer, String> appidsToNames;
    private Map<String, Integer> namesToAppids;

    public SteamGames() {
        this.num = 25;
        this.appidsToNames = null;
        this.namesToAppids = null;
    }

    public void getAppIdsAndNames() {
        JSONObject urlInfo = new JSONObject(getJson("http://api.steampowered.com/ISteamApps/GetAppList/v2"));
        JSONArray apps = urlInfo.getJSONObject("applist").getJSONArray("apps");

        Map<Integer, String> allIds = new HashMap<>();
        Map<String, Integer> allNames = new HashMap<>();

        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            allIds.put(app.getInt("appid"), app.getString("name"));
            allNames.put(app.getString("name"), app.getInt("appid"));
        }

        this.appidsToNames = allIds;
        this.namesToAppids = allNames;
    }

    public String createUrl(Iterable<Integer> appids, String cc) {
        StringBuilder appidsStr = new StringBuilder();
        for (Integer id : appids) {
            if(appidsStr.length() > 0) appidsStr.append(",");
            appidsStr.append(id.toString());
        }
        return String.format("http://store.steampowered.com/api/appdetails/?appids=%s&cc=%s&l=english&v=1", appidsStr.toString(), cc);
    }

    public Iterable<Map<String, Object>> getAll(String cc) {
        if ((appidsToNames == null) || (namesToAppids == null)) {
            getAppIdsAndNames();
        }
        // Assume batches of ids for the URL, similar approach as Python code
    }

    // Add other methods and correct the getAll implementation as needed...
}
