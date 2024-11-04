public class Arguments {
    private String[] path;
    private boolean trim = true;
    private boolean inspection;
    private boolean quiet;
    private String reporter;

    public Arguments(String[] path, boolean trim, boolean inspection, boolean quiet, String reporter) {
        this.path = path;
        this.trim = trim;
        this.inspection = inspection;
        this.quiet = quiet;
        this.reporter = reporter;
    }

    public String[] getPath() {
        return path;
    }

    public boolean isTrim() {
        return trim;
    }

    public boolean isInspection() {
        return inspection;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public String getReporter() {
        return reporter;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    public void setInspection(boolean inspection) {
        this.inspection = inspection;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }
}
