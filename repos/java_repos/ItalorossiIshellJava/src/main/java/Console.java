import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.LineReader.Option;
import org.jline.reader.Completer;
import org.jline.reader.Candidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Console {
    private static final Logger logger = LoggerFactory.getLogger(Console.class);
    private String prompt = "Prompt";
    private String promptDelim = ">";
    private String welcomeMessage = null;
    private boolean exit = false;
    private final Map<String, Command> children = new HashMap<>();

    public Console() {}

    public Console(String prompt, String promptDelim, String welcomeMessage) {
        this.prompt = prompt;
        this.promptDelim = promptDelim;
        this.welcomeMessage = welcomeMessage;
    }

    public void addChild(Command cmd) {
        children.put(cmd.getName(), cmd);
    }

    public void loop() {
        LineReader lineReader = LineReaderBuilder.builder()
                .completer(this::completer)
                .build();
        lineReader.option(Option.COMPLETE_IN_WORD, true);

        if (welcomeMessage != null) {
            System.out.println(welcomeMessage);
        }

        while (!exit) {
            try {
                String line = lineReader.readLine(prompt + promptDelim + " ");
                if (line == null || line.trim().isEmpty()) {
                    printChildrenHelp();
                } else if (line.equals("quit") || line.equals("exit")) {
                    break;
                } else {
                    walkAndRun(line);
                }
            } catch (Exception e) {
                logger.error("Exception: ", e);
                break;
            }
        }
    }

    private void walkAndRun(String command) {
        String[] tokens = command.split("\\s+");
        Command currentCommand = children.get(tokens[0]);
        if (currentCommand != null) {
            currentCommand.execute(Arrays.copyOfRange(tokens, 1, tokens.length));
        } else {
            System.out.println("Unknown Command: " + command);
            printChildrenHelp();
        }
    }

    private void printChildrenHelp() {
        System.out.println("Help:");
        for (String commandName : children.keySet()) {
            System.out.printf("%15s - %s%n", commandName, children.get(commandName).getHelp());
        }
    }

    private void completer(LineReader reader, org.jline.reader.ParsedLine line, List<Candidate> candidates) {
        String buffer = line.line();
        if (buffer.isEmpty()) {
            children.keySet().forEach(cmd -> candidates.add(new Candidate(cmd + " ")));
        } else {
            String[] tokens = buffer.split("\\s+");
            Command currentCommand = children.get(tokens[0]);
            if (currentCommand != null) {
                currentCommand.complete(Arrays.copyOfRange(tokens, 1, tokens.length), candidates, buffer);
            } else {
                children.keySet().forEach(cmd -> {
                    if (cmd.startsWith(tokens[0])) {
                        candidates.add(new Candidate(cmd + " "));
                    }
                });
            }
        }
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    // Added getter methods for the test case
    public String getPrompt() {
        return prompt;
    }

    public String getPromptDelim() {
        return promptDelim;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }
}

