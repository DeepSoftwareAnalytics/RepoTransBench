import java.util.concurrent.TimeUnit;
import java.util.Date; // Add this import statement
import java.text.SimpleDateFormat; // Also add this import statement

public class GarminConnectExporter {

    public static double paceOrSpeedRaw(int activityType, int distance, double totalTime) {
        // Cycling
        if (activityType == 2) {
            return totalTime * 3.6;
        }
        // Running
        else if (activityType == 1) {
            return 1000 / (totalTime / 60);
        }
        return 0.0;
    }

    public static String paceOrSpeedFormatted(int activityType, int distance, double totalTime) {
        if (activityType == 2) {  // Cycling
            return String.format("%.1f", paceOrSpeedRaw(activityType, distance, totalTime));
        }
        else if (activityType == 1) {  // Running
            double pace = paceOrSpeedRaw(activityType, distance, totalTime);
            long mins = (long) pace;
            long secs = Math.round((pace - mins) * 60);
            return String.format("%02d:%02d", mins, secs);
        }
        return "";
    }

    public static String trunc6(double num) {
        return String.format("%.6f", num);
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Change from java.text.SimpleDateFormat to SimpleDateFormat
        return sdf.format(date);
    }
}
