package shortuuid;

import java.util.UUID;

public class Cli {

    public static void main(String[] args) {
        if (args.length == 0) {
            // Generate random shortuuid
            ShortUUID su = new ShortUUID();
            System.out.println(su.uuid());
            return;
        }

        String command = args[0];
        switch (command) {
            case "encode":
                if (args.length != 2) {
                    System.out.println("Usage: encode <UUID>");
                    return;
                }
                encode(args[1]);
                break;

            case "decode":
                if (args.length < 2 || args.length > 3) {
                    System.out.println("Usage: decode <shortUUID> [--legacy]");
                    return;
                }
                boolean legacy = args.length == 3 && args[2].equals("--legacy");
                decode(args[1], legacy);
                break;

            default:
                System.out.println("Unknown command: " + command);
                break;
        }
    }

    private static void encode(String uuidStr) {
        UUID uuid = UUID.fromString(uuidStr);
        ShortUUID su = new ShortUUID();
        System.out.println(su.encode(uuid));
    }

    private static void decode(String shortUUID, boolean legacy) {
        ShortUUID su = new ShortUUID();
        System.out.println(su.decode(shortUUID, legacy));
    }
}
