package com.example.prodict;

import java.util.*;
import java.util.stream.Collectors;

public class Prodict extends HashMap<String, Object> {
    private static final Set<String> DICT_RESERVED_KEYS = new HashSet<>(Arrays.asList(
        "compute", "computeIfAbsent", "computeIfPresent", "containsKey", "containsValue", "entrySet",
        "equals", "forEach", "get", "getClass", "keySet", "merge", "put", "putAll", "putIfAbsent",
        "remove", "replace", "replaceAll", "size", "values"));

    public Prodict() {
        super();
    }

    public Prodict(Map<String, Object> map) {
        this.putAll(map);
    }

    public static Prodict fromDict(Map<String, Object> map) {
        return new Prodict(map);
    }

    public void init() {
        // Initialize if necessary
    }

    public Map<String, Object> toDict(boolean isRecursive, boolean excludeNone, boolean excludeNoneInLists) {
        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : this.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (excludeNone && v == null) {
                continue;
            }
            if (v instanceof Prodict && isRecursive) {
                ret.put(k, ((Prodict) v).toDict(isRecursive, excludeNone, excludeNoneInLists));
            } else if (v instanceof List && excludeNoneInLists) {
                List<Object> list = ((List<?>) v).stream()
                        .map(item -> (item instanceof Prodict) ? ((Prodict) item).toDict(isRecursive, true, excludeNoneInLists) : item)
                        .collect(Collectors.toList());
                ret.put(k, list);
            } else {
                ret.put(k, v);
            }
        }
        return ret;
    }

    public void setAttribute(String attrName, Object value) {
        if (DICT_RESERVED_KEYS.contains(attrName)) {
            throw new IllegalArgumentException("You cannot set a reserved name as attribute");
        }
        this.put(attrName, value);
    }

    public void setAttributes(Map<String, Object> attributes) {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            setAttribute(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object get(Object key) {
        return super.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return super.put(key, value);
    }

    @Override
    public Set<String> keySet() {
        return super.keySet();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return super.entrySet();
    }

    @Override
    public Collection<Object> values() {
        return super.values();
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    public static void main(String[] args) {
        // Example 0
        Prodict p = new Prodict();
        p.setAttribute("lang", "Java");
        p.setAttribute("pros", "Rocks!");
        System.out.println(p);  // {lang=Java, pros=Rocks!}

        // Example 1
        class Country extends Prodict {
            public String name;
            public int population;

            public Country() {
                super();
            }

            public Country(Map<String, Object> map) {
                super(map);
                if (map.containsKey("name")) this.name = (String) map.get("name");
                if (map.containsKey("population")) this.population = (int) map.get("population");
            }
        }

        Country turkey = new Country();
        turkey.setAttribute("name", "Turkey");
        turkey.setAttribute("population", 79814871);

        // Example 2
        Country germany = new Country(new HashMap<String, Object>() {{
            put("name", "Germany");
            put("population", 82175700);
            put("flag_colors", Arrays.asList("black", "red", "yellow"));
        }});

        System.out.println(germany.get("population"));  // 82175700
        System.out.println(germany.get("flag_colors"));  // [black, red, yellow]

        // Example 3
        class Ram extends Prodict {
            public int capacity;
            public String unit;
            public String type;
            public int clock;

            public Ram() {
                super();
            }

            public Ram(Map<String, Object> map) {
                super(map);
                if (map.containsKey("capacity")) this.capacity = (int) map.get("capacity");
                if (map.containsKey("unit")) this.unit = (String) map.get("unit");
                if (map.containsKey("type")) this.type = (String) map.get("type");
                if (map.containsKey("clock")) this.clock = (int) map.get("clock");
            }
        }

        class Computer extends Prodict {
            public String name;
            public int cpu_cores;
            public List<Ram> rams;

            public Computer() {
                super();
                this.rams = new ArrayList<>();
            }

            public Computer(Map<String, Object> map) {
                super(map);
                if (map.containsKey("name")) this.name = (String) map.get("name");
                if (map.containsKey("cpu_cores")) this.cpu_cores = (int) map.get("cpu_cores");
                if (map.containsKey("rams")) {
                    List<Map<String, Object>> ramsList = (List<Map<String, Object>>) map.get("rams");
                    this.rams = ramsList.stream().map(Ram::new).collect(Collectors.toList());
                } else {
                    this.rams = new ArrayList<>();
                }
            }

            public int totalRam() {
                return this.rams.stream().mapToInt(ram -> ram.capacity).sum();
            }
        }

        Computer comp1 = new Computer(new HashMap<String, Object>() {{
            put("name", "My Computer");
            put("cpu_cores", 4);
            put("rams", Arrays.asList(
                    new HashMap<String, Object>() {{
                        put("capacity", 4);
                        put("unit", "GB");
                        put("type", "DDR3");
                        put("clock", 2400);
                    }}
            ));
        }});

        System.out.println(comp1.rams);  // [{capacity=4, unit=GB, type=DDR3, clock=2400}]

        comp1.rams.add(new Ram(new HashMap<String, Object>() {{
            put("capacity", 8);
            put("type", "DDR3");
        }}));

        comp1.rams.add(new Ram(new HashMap<String, Object>() {{
            put("capacity", 12);
            put("type", "DDR3");
            put("clock", 2400);
        }}));

        System.out.println(comp1.rams);
        // [{capacity: 4, unit: 'GB', type: 'DDR3', clock: 2400},
        //  {capacity: 8, type: 'DDR3'},
        //  {capacity: 12, type: 'DDR3', clock: 2400}]

        System.out.println(comp1.rams.getClass());
        System.out.println(comp1.rams.get(0).getClass());
    }
}
