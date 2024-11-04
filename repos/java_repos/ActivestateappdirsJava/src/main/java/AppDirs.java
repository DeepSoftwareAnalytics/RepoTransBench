import java.util.Optional;

public class AppDirs {

    public static final String version = "1.4.4";
    public static final String versionInfo = "1.4.4";

    private final String appname;
    private final String appauthor;
    private final String versionValue;
    private final boolean roaming;
    private final boolean multipath;

    public AppDirs(String appname, String appauthor, String version, boolean roaming, boolean multipath) {
        this.appname = appname;
        this.appauthor = appauthor;
        this.versionValue = version;
        this.roaming = roaming;
        this.multipath = multipath;
    }

    public String userDataDir(String appName, String appCompany) {
        return AppDirsHelpers.userDataDir(appName, appCompany, versionValue, roaming);
    }

    public String siteDataDir(String appName, String appCompany) {
        return AppDirsHelpers.siteDataDir(appName, appCompany, versionValue, multipath);
    }

    public String userCacheDir(String appName, String appCompany) {
        return AppDirsHelpers.userCacheDir(appName, appCompany, versionValue, true);
    }

    public String userConfigDir(String appName, String appCompany) {
        return AppDirsHelpers.userConfigDir(appName, appCompany, versionValue, roaming);
    }

    public String siteConfigDir(String appName, String appCompany) {
        return AppDirsHelpers.siteConfigDir(appName, appCompany, versionValue, multipath);
    }

    public String userStateDir(String appName, String appCompany) {
        return AppDirsHelpers.userStateDir(appName, appCompany, versionValue, roaming);
    }

    public String userLogDir(String appName, String appCompany) {
        return AppDirsHelpers.userLogDir(appName, appCompany, versionValue, true);
    }
}
