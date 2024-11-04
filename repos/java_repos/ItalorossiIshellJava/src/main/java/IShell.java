public class IShell {
    public static void main(String[] args) {
        Console console = new Console("root@dev:~", "#", "Welcome to IShell");
        Command ls = new Command("ls", "List directory contents") {
            @Override
            protected void run(String line) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("ls", "-l");
                    Process process = pb.start();
                    process.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Command bash = new Command("bash", "Start a new bash session") {
            @Override
            protected void run(String line) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("bash");
                    pb.inheritIO().start().waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Command top = new Command("top", "Display Linux tasks") {
            @Override
            protected void run(String line) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("top");
                    pb.inheritIO().start().waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        console.addChild(ls);
        console.addChild(bash);
        console.addChild(top);
        console.loop();
    }
}
