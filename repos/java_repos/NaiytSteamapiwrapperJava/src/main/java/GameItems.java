import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GameItems extends SteamAPI {

    private Map<String, Map<String, Object>> tf2Items;
    private Map<String, Map<String, Object>> dota2Items;

    public GameItems(String apiKey) {
        super("", apiKey);
        this.tf2Items = null;
        this.dota2Items = null;
    }

    private Map<String, Map<String, Object>> getItems(String game, boolean rawJson) {
        String url = String.format("http://api.steampowered.com/IEconItems_%s/GetSchema/v0001/?key=%s", game, apiKey);
        JSONObject jsonData = new JSONObject(getJson(url));

        if (rawJson) {
            return jsonData.getJSONObject("result").toMap().get("items");
        }

        Map<String, Map<String, Object>> allItems = new HashMap<>();

        JSONArray itemsArray = jsonData.getJSONObject("result").getJSONArray("items");
        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject item = itemsArray.getJSONObject(i);
            Map<String, Object> values = new HashMap<>();
            values.put("defindex", item.getInt("defindex"));
            values.put("item_class", item.getString("item_class"));
            values.put("item_type_name", item.getString("item_type_name"));
            // Optional fields are handled similarly...
            
            allItems.put(item.getString("name"), values);
        }

        return allItems;
    }

    public Map<String, Map<String, Object>> getAll(String game, boolean rawJson) {
        if (game.equalsIgnoreCase("tf2")) {
            if (tf2Items == null) {
                tf2Items = getItems("440", rawJson);
            }
            return tf2Items;
        } else if (game.equalsIgnoreCase("dota2")) {
            if (dota2Items == null) {
                dota2Items = getItems("570", rawJson);
            }
            return dota2Items;
        } else {
            throw new IllegalArgumentException("Please enter either TF2 or Dota2");
        }
    }
}
