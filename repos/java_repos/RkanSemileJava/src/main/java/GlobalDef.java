public class GlobalDef {
    private static boolean useReducedTime = false;
    private static boolean longerTimeFirst = false;
    private static boolean removeNoMsgEntry = false;
    private static int maxBranch = 10;
    private static int maxMsgLength = 100;
    private static boolean traverse = false;

    public static boolean useReducedTime() {
        return useReducedTime;
    }

    public static void setUseReducedTime(boolean useReducedTime) {
        GlobalDef.useReducedTime = useReducedTime;
    }

    public static boolean longerTimeFirst() {
        return longerTimeFirst;
    }

    public static void setLongerTimeFirst(boolean longerTimeFirst) {
        GlobalDef.longerTimeFirst = longerTimeFirst;
    }

    public static boolean removeNoMsgEntry() {
        return removeNoMsgEntry;
    }

    public static void setRemoveNoMsgEntry(boolean removeNoMsgEntry) {
        GlobalDef.removeNoMsgEntry = removeNoMsgEntry;
    }

    public static int getMaxBranch() {
        return maxBranch;
    }

    public static void setMaxBranch(int maxBranch) {
        GlobalDef.maxBranch = maxBranch;
    }

    public static int getMaxMsgLength() {
        return maxMsgLength;
    }

    public static void setMaxMsgLength(int maxMsgLength) {
        GlobalDef.maxMsgLength = maxMsgLength;
    }

    public static boolean isTraverse() {
        return traverse;
    }

    public static void setTraverse(boolean traverse) {
        GlobalDef.traverse = traverse;
    }
}
