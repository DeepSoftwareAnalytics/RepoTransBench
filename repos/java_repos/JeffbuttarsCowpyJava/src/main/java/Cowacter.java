import java.util.HashMap;
import java.util.Map;

public class Cowacter {
    protected String eyes;
    protected boolean thoughts;
    protected boolean tongue;
    protected String body;
    
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

    public Cowacter() {
        this("default", false, false, "     \\   ^__^\n      \\  (oo)\\_______\n         (__)\\       )\\/\\\n          ||----w |\n           ||     ||");
    }

    public Cowacter(String eyes, boolean thoughts, boolean tongue, String body) {
        this.eyes = EYES.getOrDefault(eyes, "oo");
        this.thoughts = thoughts;
        this.tongue = tongue;
        this.body = body;
    }
    
    public Cowacter withEyes(String eyes) {
        this.eyes = EYES.getOrDefault(eyes, "oo");
        return this;
    }

    public Cowacter withThoughts(boolean thoughts) {
        this.thoughts = thoughts;
        return this;
    }

    public Cowacter withTongue(boolean tongue) {
        this.tongue = tongue;
        return this;
    }

    private String bubble(String message) {
        String[] lines = message.split("\n");
        int contentLength = 0;
        for (String line : lines) {
            contentLength = Math.max(contentLength, line.length());
        }
        int borderLength = contentLength + 2;
        StringBuilder bubble = new StringBuilder();

        bubble.append(" ").append("_".repeat(borderLength)).append(" \n");

        if (lines.length > 1) {
            bubble.append(String.format("/ %-" + contentLength + "s \\\n", lines[0]));
            for (int i = 1; i < lines.length - 1; i++) {
                bubble.append(String.format("| %-" + contentLength + "s |\n", lines[i]));
            }
            bubble.append(String.format("\\ %-" + contentLength + "s /\n", lines[lines.length - 1]));
        } else {
            bubble.append(String.format("< %-" + contentLength + "s >\n", lines[0]));
        }

        bubble.append(" ").append("-".repeat(borderLength)).append(" \n");
        return bubble.toString();
    }
    
    public String milk(String msg) {
        msg = msg.trim();
        if (msg.isEmpty()) {
            msg = String.format("%s, eyes:%s, tongue:%b, thoughts:%b",
                this.getClass().getSimpleName(),
                this.eyes,
                this.tongue,
                this.thoughts
            );
        }
        try {
            String bubbleMessage = bubble(msg);
            String thoughtsSymbol = thoughts ? "o" : "\\";
            String tongueSymbol = tongue ? "U " : "";
            return bubbleMessage + String.format(this.body,
                thoughtsSymbol,
                eyes,
                tongueSymbol
            );
        } catch (Exception e) {
            return "Unable to print the message :(\n" + e.getMessage();
        }
    }
}
