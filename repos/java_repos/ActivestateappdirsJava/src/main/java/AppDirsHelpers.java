import java.util.Optional;

public class AppDirsHelpers {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static boolean isWindows() {
        return OS.contains("win");
    }

    private static boolean isMac() {
        return OS.contains("mac");
    }

    private static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    public static String userDataDir(String appname, String appauthor, String version, boolean roaming) {
        if (isWindows()) {
            if (appauthor == null) {
                appauthor = appname;
            }
            String constVal = roaming ? "CSIDL_APPDATA" : "CSIDL_LOCAL_APPDATA";
            String path = getWindowsFolder(constVal);
            path = appendPath(appname, appauthor, version, path);
            return path;
        } else if (isMac()) {
            String path = System.getProperty("user.home") + "/Library/Application Support";
            if (appname != null) {
                path = path + "/" + appname;
            }
            path = appendVersion(version, path);
            return path;
        } else if (isUnix()) {
            String path = Optional.ofNullable(System.getenv("XDG_DATA_HOME"))
                    .orElse(System.getProperty("user.home") + "/.local/share");
            if (appname != null) {
                path = path + "/" + appname;
            }
            path = appendVersion(version, path);
            return path;
        }
        return null;
    }

    public static String siteDataDir(String appname, String appauthor, String version, boolean multipath) {
        if (isWindows()) {
            if (appauthor == null) {
                appauthor = appname;
            }
            String path = getWindowsFolder("CSIDL_COMMON_APPDATA");
            path = appendPath(appname, appauthor, version, path);
            return path;
        } else if (isMac()) {
            String path = "/Library/Application Support";
            if (appname != null) {
                path = path + "/" + appname;
            }
            path = appendVersion(version, path);
            return path;
        } else if (isUnix()) {
            String paths = Optional.ofNullable(System.getenv("XDG_DATA_DIRS"))
                    .orElse("/usr/local/share:/usr/share");
            String[] pathList = paths.split(":");
            if (appname != null) {
                for (int i = 0; i < pathList.length; i++) {
                    pathList[i] = pathList[i] + "/" + appname;
                    if (version != null) {
                        pathList[i] = pathList[i] + "/" + version;
                    }
                }
            }
            if (multipath) {
                return String.join(":", pathList);
            } else {
                return pathList[0];
            }
        }
        return null;
    }

    public static String userConfigDir(String appname, String appauthor, String version, boolean roaming) {
        if (isWindows()) {
            return userDataDir(appname, appauthor, version, roaming);
        } else if (isMac()) {
            String path = System.getProperty("user.home") + "/Library/Preferences";
            if (appname != null) {
                path = path + "/" + appname;
            }
            path = appendVersion(version, path);
            return path;
        } else if (isUnix()) {
            String path = Optional.ofNullable(System.getenv("XDG_CONFIG_HOME"))
                    .orElse(System.getProperty("user.home") + "/.config");
            if (appname != null) {
                path = path + "/" + appname;
            }
            path = appendVersion(version, path);
            return path;
        }
        return null;
    }

    public static String siteConfigDir(String appname, String appauthor, String version, boolean multipath) {
        if (isWindows()) {
            String path = siteDataDir(appname, appauthor, version, multipath);
            return path;
        } else if (isMac()) {
            return siteDataDir(appname, appauthor, version, multipath);
        } else if (isUnix()) {
            String paths = Optional.ofNullable(System.getenv("XDG_CONFIG_DIRS"))
                    .orElse("/etc/xdg");
            String[] pathList = paths.split(":");
            if (appname != null) {
                for (int i = 0; i < pathList.length; i++) {
                    pathList[i] = pathList[i] + "/" + appname;
                    if (version != null) {
                        pathList[i] = pathList[i] + "/" + version;
                    }
                }
            }
            if (multipath) {
                return String.join(":", pathList);
            } else {
                return pathList[0];
            }
        }
        return null;
    }

    public static String userCacheDir(String appname, String appauthor, String version, boolean opinion) {
        if (isWindows()) {
            if (appauthor == null) {
                appauthor = appname;
            }
            String path = getWindowsFolder("CSIDL_LOCAL_APPDATA");
            path = appendPath(appname, appauthor, version, path);
            if (opinion) {
                path = path + "/Cache";
            }
            return path;
        } else if (isMac()) {
            String path = System.getProperty("user.home") + "/Library/Caches";
            if (appname != null) {
                path = path + "/" + appname;
            }
            path = appendVersion(version, path);
            return path;
        } else if (isUnix()) {
            String path = Optional.ofNullable(System.getenv("XDG_CACHE_HOME"))
                    .orElse(System.getProperty("user.home") + "/.cache");
            if (appname != null) {
                path = path + "/" + appname;
            }
            path = appendVersion(version, path);
            return path;
        }
        return null;
    }

    public static String userStateDir(String appname, String appauthor, String version, boolean roaming) {
        if (isWindows() || isMac()) {
            return userDataDir(appname, appauthor, version, roaming);
        } else if (isUnix()) {
            String path = Optional.ofNullable(System.getenv("XDG_STATE_HOME"))
                    .orElse(System.getProperty("user.home") + "/.local/state");
            if (appname != null) {
                path = path + "/" + appname;
            }
            path = appendVersion(version, path);
            return path;
        }
        return null;
    }

    public static String userLogDir(String appname, String appauthor, String version, boolean opinion) {
        if (isMac()) {
            String path = System.getProperty("user.home") + "/Library/Logs";
            if (appname != null) {
                path = path + "/" + appname;
            }
            path = appendVersion(version, path);
            return path;
        } else if (isWindows()) {
            String path = userDataDir(appname, appauthor, version, true);
            path = appendVersion(null, path);
            if (opinion) {
                path = path + "/Logs";
            }
            return path;
        } else if (isUnix()) {
            String path = userCacheDir(appname, appauthor, version, true);
            path = appendVersion(null, path);
            if (opinion) {
                path = path + "/log";
            }
            return path;
        }
        return null;
    }

    private static String getWindowsFolder(String constVal) {
        // For simplicity, let's mock the logic
        // Ideally, this should interact with Windows APIs via JNA/JNI
        switch (constVal) {
            case "CSIDL_APPDATA":
                return System.getenv("APPDATA");
            case "CSIDL_LOCAL_APPDATA":
                return System.getenv("LOCALAPPDATA");
            case "CSIDL_COMMON_APPDATA":
                return System.getenv("ProgramData");
            default:
                return null;
        }
    }

    private static String appendPath(String appname, String appauthor, String version, String path) {
        if (appname != null) {
            path = appauthor != null ? path + "/" + appauthor + "/" + appname : path + "/" + appname;
            path = appendVersion(version, path);
        }
        return path;
    }

    private static String appendVersion(String version, String path) {
        if (version != null) {
            path = path + "/" + version;
        }
        return path;
    }
}
