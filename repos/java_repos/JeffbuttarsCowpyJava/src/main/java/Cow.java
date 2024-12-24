import java.util.*;
import java.util.logging.*;

public class Cow {
    private static final Logger logger = Logger.getLogger("cowpy");
    
    static {
        // Set up the logger
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.INFO);
        ch.setFormatter(new java.util.logging.Formatter() { // Updated to specifically use java.util.logging.Formatter
            public String format(LogRecord record) {
                return String.format("%s: %s %s %s %s -- %s%n",
                        record.getLevel(),
                        new Date(record.getMillis()),
                        record.getThreadID(),
                        record.getSourceClassName(),
                        record.getSourceMethodName(),
                        record.getMessage());
            }
        });
        logger.addHandler(ch);
        logger.setLevel(Level.INFO);
    }

    private static final Map<String, String> EYES = new HashMap<String, String>() {{
        put("default", "oo");
        put("borg", "==");
        put("dead", "xx");
        put("greedy", "$$");
        put("paranoid", "@@");
        put("stoned", "**");
        put("tired", "--");
        put("wired", "OO");
        put("young", "..");
    }};
    
    private static final Set<String> NOT_SAFE_FOR_WORK_COWACTERS = 
        new HashSet<>(Arrays.asList("bongcow", "sodomized", "headincow", "telebears"));

    private static final Set<String> NOT_SAFE_FOR_WORK_EYES = 
        new HashSet<>(Collections.singletonList("stoned"));
    
    private static final Map<String, Cowacter> COWACTERS = new HashMap<String, Cowacter>() {{
        put("default", new Cowacter());
        put("moose", new Moose());
        // Add other cowacters as needed
    }};

    public static List<String> getCowacters(boolean sfw, boolean sort) {
        Set<String> cowKeys = new HashSet<>(COWACTERS.keySet());
        if (sfw) {
            cowKeys.removeAll(NOT_SAFE_FOR_WORK_COWACTERS);
        }
        List<String> cows = new ArrayList<>(cowKeys);
        if (sort) {
            Collections.sort(cows);
        }
        return cows;
    }
    
    public static List<String> getEyes(boolean sfw, boolean sort) {
        Set<String> eyeKeys = new HashSet<>(EYES.keySet());
        if (sfw) {
            eyeKeys.removeAll(NOT_SAFE_FOR_WORK_EYES);
        }
        List<String> eyes = new ArrayList<>(eyeKeys);
        if (sort) {
            Collections.sort(eyes);
        }
        return eyes;
    }
    
    public static boolean notSafeForWork(String cow, String eyes) {
        return NOT_SAFE_FOR_WORK_COWACTERS.contains(cow) || NOT_SAFE_FOR_WORK_EYES.contains(eyes);
    }
    
    public static Cowacter getCow(String name) {
        return COWACTERS.getOrDefault(name, new Cowacter());
    }
    
    public static Set<String> eyeOptions() {
        return EYES.keySet();
    }
    
    public static Set<String> cowOptions() {
        return COWACTERS.keySet();
    }
    
    public static String milkRandomCow(String msg, boolean sfw) {
        List<String> cows = getCowacters(sfw, false);
        List<String> eyes = getEyes(sfw, false);
        
        Cowacter cow = COWACTERS.get(cows.get(new Random().nextInt(cows.size())));
        String eye = eyes.get(new Random().nextInt(eyes.size()));
        
        return cow.withEyes(eye)
                  .withTongue(new Random().nextBoolean())
                  .withThoughts(new Random().nextBoolean())
                  .milk(msg);
    }
}

