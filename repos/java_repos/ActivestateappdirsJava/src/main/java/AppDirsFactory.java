public class AppDirsFactory {

    private static AppDirs instance;

    private AppDirsFactory() {
        // private constructor to enforce singleton pattern
    }

    public static AppDirs getInstance() {
        if (instance == null) {
            instance = new AppDirs(null, null, null, false, false);
        }
        return instance;
    }
}
