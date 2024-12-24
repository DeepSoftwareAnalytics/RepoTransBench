import org.jline.reader.Candidate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

public class Command extends Console {
    private final String name;
    private final String help;
    private final boolean dynamicArgs;
    private Supplier<List<String>> argsSupplier;
    protected final Map<String, Command> children = new HashMap<>();

    public Command(String name) {
        this(name, "No help provided", false);
    }

    public Command(String name, boolean dynamicArgs) {
        this(name, "No help provided", dynamicArgs);
    }

    public Command(String name, String help) {
        this(name, help, false);
    }

    public Command(String name, String help, boolean dynamicArgs) {
        this.name = name;
        this.help = help;
        this.dynamicArgs = dynamicArgs;
    }

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public boolean isDynamicArgs() {
        return dynamicArgs;
    }

    public void setArgs(Supplier<List<String>> argsSupplier) {
        this.argsSupplier = argsSupplier;
    }

    public void addChild(Command cmd) {
        children.put(cmd.getName(), cmd);
    }

    public void complete(String[] line, List<Candidate> candidates, String buffer) {
        if (line != null && line.length > 0 && dynamicArgs && line.length > 1) {
            if (argsSupplier != null) {
                List<String> args = argsSupplier.get();
                for (String arg : args) {
                    if (arg.startsWith(line[line.length - 1])) {
                        candidates.add(new Candidate(arg + " "));
                    }
                }
            } else {
                candidates.add(new Candidate("argument "));
            }
        } else if (line != null && line.length > 0) {
            if (line.length == 1) {
                for (String cmd : children.keySet()) {
                    if (cmd.startsWith(line[0])) {
                        candidates.add(new Candidate(cmd + " "));
                    }
                }
            } else {
                String nextCommand = line[0];
                Command candidateCommand = children.get(nextCommand);
                if (candidateCommand != null) {
                    candidateCommand.complete(line.length > 1 ? java.util.Arrays.copyOfRange(line, 1, line.length) : null, candidates, buffer);
                }
            }
        } else {
            for (String cmd : children.keySet()) {
                candidates.add(new Candidate(cmd + " "));
            }
        }
    }

    public void execute(String[] args) {
        if (args.length > 0) {
            String firstArg = args[0];
            Command childCommand = children.get(firstArg);
            if (childCommand != null) {
                childCommand.execute(java.util.Arrays.copyOfRange(args, 1, args.length));
            } else if (dynamicArgs) {
                run(String.join(" ", args));
            } else {
                System.out.println("Command not found: " + firstArg);
                printChildrenHelp();
            }
        } else {
            run(name);
        }
    }

    protected void run(String line) {
        System.out.printf("Executing %s with args: %s%n", name, line);
    }

    protected void printChildrenHelp() {
        System.out.println("Help:");
        for (String commandName : children.keySet()) {
            System.out.printf("%15s - %s%n", commandName, children.get(commandName).getHelp());
        }
    }
}
