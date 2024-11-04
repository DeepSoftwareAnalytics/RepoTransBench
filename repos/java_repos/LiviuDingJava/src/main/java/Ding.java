import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.concurrent.TimeUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Ding {
    public static final String VERSION = "2.1.0";
    public static final int N_BEEPS = 4;
    public static final double WAIT_BEEPS = 0.15;

    public static void main(String[] args) {
        DingArgs dingArgs = getArgs(args);
        while (true) {
            try {
                int seconds;
                seconds = parseTimeSeconds(dingArgs);
                countdown(seconds, dingArgs.noTimer);
                beep(seconds, dingArgs.command);
                if (!"every".equals(dingArgs.mode)) {
                    break;
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public static DingArgs getArgs(String[] args) {
        DingArgs dingArgs = new DingArgs();
        dingArgs.parseArgs(args);
        return dingArgs;
    }

    public static void countdown(int seconds, boolean notimer) throws InterruptedException {
        if (!notimer) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
        while (seconds > 0) {
            long start = System.currentTimeMillis();
            if (!notimer) {
                System.out.printf("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
                System.out.flush();
            }
            seconds--;
            Thread.sleep(Math.max(0, 1000 - (System.currentTimeMillis() - start)));
            if (!notimer) {
                System.out.print("\r");
            }
        }
    }

    public static void beep(int seconds, String command) {
        for (int i = 0; i < N_BEEPS; i++) {
            if (command != null) {
                try {
                    Runtime.getRuntime().exec(command);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.print("\007");
                System.out.flush();
            }
            try {
                Thread.sleep((long) (WAIT_BEEPS * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static int parseTimeSeconds(DingArgs args) {
        if ("in".equals(args.mode) || "every".equals(args.mode)) {
            return TimeParser.getSecondsRelative(args.time);
        } else {
            return TimeParser.getSecondsAbsolute(args.time[0]);  // Fix the incompatible types: java.lang.String[] cannot be converted to java.lang.String
        }
    }
}
