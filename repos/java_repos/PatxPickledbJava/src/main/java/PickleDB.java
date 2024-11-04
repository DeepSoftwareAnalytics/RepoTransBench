import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.*;

public class PickleDB {
    private Map<String, Object> db = new HashMap<>();
    private String location;
    private boolean autoDump;
    private Thread dumpThread;

    public PickleDB(String location, boolean autoDump, boolean useSigTermHandler) {
        this.location = location;
        this.autoDump = autoDump;

        if (Files.exists(Paths.get(location))) {
            _loadDB();
        } else {
            db.clear();
        }

        if (useSigTermHandler) {
            setSigTermHandler();
        }
    }

    public static PickleDB load(String location, boolean autoDump) {
        return new PickleDB(location, autoDump, true);
    }

    public void setSigTermHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dumpThread != null) {
                try {
                    dumpThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }));
    }

    private void _loadDB() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(location)));
            db = new JSONObject(content).toMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void _dump() {
        String jsonString = new JSONObject(db).toString();
        Path tempFile;
        try {
            tempFile = Files.createTempFile("temp", null);
            Files.write(tempFile, jsonString.getBytes());
            if (Files.size(tempFile) > 0) {
                Files.move(tempFile, Paths.get(location), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean dump() {
        dumpThread = new Thread(this::_dump);
        dumpThread.start();
        try {
            dumpThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    private void _autoDumpDB() {
        if (autoDump) {
            dump();
        }
    }

    public boolean set(String key, Object value) {
        db.put(key, value);
        _autoDumpDB();
        return true;
    }

    public Object get(String key) {
        return db.getOrDefault(key, false);
    }

    public Set<String> getAll() {
        return db.keySet();
    }

    public boolean exists(String key) {
        return db.containsKey(key);
    }

    public boolean rem(String key) {
        if (!exists(key)) return false;
        db.remove(key);
        _autoDumpDB();
        return true;
    }

    public boolean append(String key, String more) {
        String temp = (String) db.get(key);
        db.put(key, temp + more);
        _autoDumpDB();
        return true;
    }

    public boolean lcreate(String name) {
        db.put(name, new ArrayList<>());
        _autoDumpDB();
        return true;
    }

    public boolean ladd(String name, Object value) {
        List<Object> list = (List<Object>) db.get(name);
        list.add(value);
        _autoDumpDB();
        return true;
    }

    public List<Object> lgetall(String name) {
        return (List<Object>) db.get(name);
    }

    public List<Object> lrange(String name, int start, int end) {
        List<Object> list = (List<Object>) db.get(name);
        return list.subList(start, end);
    }

    public boolean lexists(String name, Object value) {
        List<Object> list = (List<Object>) db.get(name);
        return list.contains(value);
    }

    public boolean dcreate(String name) {
        db.put(name, new HashMap<String, Object>());
        _autoDumpDB();
        return true;
    }

    public boolean dadd(String name, String key, Object value) {
        Map<String, Object> dict = (Map<String, Object>) db.get(name);
        dict.put(key, value);
        _autoDumpDB();
        return true;
    }

    public Map<String, Object> dgetall(String name) {
        return (Map<String, Object>) db.get(name);
    }

    public Object dget(String name, String key) {
        Map<String, Object> dict = (Map<String, Object>) db.get(name);
        return dict.get(key);
    }

    public boolean dexists(String name, String key) {
        Map<String, Object> dict = (Map<String, Object>) db.get(name);
        return dict.containsKey(key);
    }
    
    public boolean deldb() {
        db.clear();
        _autoDumpDB();
        return true;
    }
}
