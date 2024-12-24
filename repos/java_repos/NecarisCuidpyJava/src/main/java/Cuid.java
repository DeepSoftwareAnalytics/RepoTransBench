import java.net.InetAddress;
import java.net.UnknownHostException;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Random;

public class Cuid {
    public static final int BASE = 36;
    public static final int BLOCK_SIZE = 4;
    public static final long DISCRETE_VALUES = (long) Math.pow(BASE, BLOCK_SIZE);

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final String PADDING = "000000000";

    private static CuidGenerator generator = null;

    public static CuidGenerator getGenerator() {
        if (generator == null) {
            generator = new CuidGenerator();
        }
        return generator;
    }

    public static String toBase36(int number) {
        StringBuilder chars = new StringBuilder();
        while (number != 0) {
            int remainder = number % 36;
            chars.insert(0, ALPHABET.charAt(remainder));
            number = number / 36;
        }
        return chars.length() > 0 ? chars.toString() : "0";
    }

    public static String pad(String string, int size) {
        int strlen = string.length();
        if (strlen == size) {
            return string;
        }
        if (strlen < size) {
            return PADDING.substring(0, size - strlen) + string;
        }
        return string.substring(strlen - size);
    }

    public static String randomBlock() {
        Random random = new Random();
        int randomNumber = random.nextInt((int) DISCRETE_VALUES);
        String randomString = toBase36(randomNumber);
        return pad(randomString, BLOCK_SIZE);
    }

    public static String getProcessFingerprint() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        long pid = Long.parseLong(processName.split("@")[0]);
        String hostname = "unknown";
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            System.err.println("Hostname can not be resolved");
        }
        int hostnameHash = hostname.chars().sum() + hostname.length() + 36;
        String paddedPid = pad(toBase36((int)pid), 2);
        String paddedHostname = pad(toBase36(hostnameHash), 2);
        return paddedPid + paddedHostname;
    }

    public static String cuid() {
        return getGenerator().cuid();
    }

    public static String slug() {
        return getGenerator().slug();
    }
}
