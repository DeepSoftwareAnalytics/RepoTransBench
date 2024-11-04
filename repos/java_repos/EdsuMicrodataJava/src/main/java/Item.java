import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item {
    private List<URI> itemtype;
    private URI itemid;
    private Map<String, List<Object>> properties = new HashMap<>();

    public Item(String itemtype, String itemid) {
        if (itemtype != null && !itemtype.isEmpty()) {
            String[] types = itemtype.split(" ");
            this.itemtype = new ArrayList<>();
            for(String type : types) {
                this.itemtype.add(new URI(type));
            }
        }
        if (itemid != null) {
            this.itemid = new URI(itemid);
        }
    }

    public List<URI> getItemtype() {
        return itemtype;
    }

    public URI getItemid() {
        return itemid;
    }

    public Map<String, List<Object>> getProperties() {
        return properties;
    }

    public void set(String name, Object value) {
        if (!properties.containsKey(name)) {
            properties.put(name, new ArrayList<>());
        }
        properties.get(name).add(value);
    }

    public Object get(String name) {
        List<Object> values = properties.get(name);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

    public List<Object> getAll(String name) {
        return properties.getOrDefault(name, new ArrayList<>());
    }

    public String toJson() {
        Map<String, Object> jsonMap = new HashMap<>();
        if (itemtype != null && !itemtype.isEmpty()) {
            List<String> types = new ArrayList<>();
            for (URI uri : itemtype) {
                types.add(uri.getUri());
            }
            jsonMap.put("type", types);
        }
        if (itemid != null) {
            jsonMap.put("id", itemid.getUri());
        }
        jsonMap.put("properties", properties);
        Gson gson = new Gson();
        return gson.toJson(jsonMap);
    }
    
    public static Item fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Item.class);
    }
}

