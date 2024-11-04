public class Subentry {
    public final Code code;
    public final int callcount;
    public final int reccallcount;
    public final double inlinetime;
    public final double totaltime;

    public Subentry(Code code, int callcount, int reccallcount, double inlinetime, double totaltime) {
        this.code = code;
        this.callcount = callcount;
        this.reccallcount = reccallcount;
        this.inlinetime = inlinetime;
        this.totaltime = totaltime;
    }

    @Override
    public String toString() {
        return "<Subentry: " + code + ", " + callcount + ", " + reccallcount + ", " + inlinetime + ", " + totaltime + ">";
    }
}
