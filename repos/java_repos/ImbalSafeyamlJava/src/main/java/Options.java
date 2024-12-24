public class Options {
    public boolean fixUnquoted = false;
    public boolean fixNoSpace = false;
    public boolean forceStringKeys = false;
    public boolean forceCommas = false;

    public Options(boolean fixUnquoted, boolean fixNoSpace, boolean forceStringKeys, boolean forceCommas) {
        this.fixUnquoted = fixUnquoted;
        this.fixNoSpace = fixNoSpace;
        this.forceStringKeys = forceStringKeys;
        this.forceCommas = forceCommas;
    }
}
