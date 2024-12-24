import java.util.List;

public class Entry {
    public final Code code;
    public final int callcount;
    public final int reccallcount;
    public final double inlinetime;
    public final double totaltime;
    public final List<Subentry> calls;

    public Entry(Code code, int callcount, int reccallcount, double inlinetime, double totaltime, List<Subentry> calls) {
        this.code = code;
        this.callcount = callcount;
        this.reccallcount = reccallcount;
        this.inlinetime = inlinetime;
        this.totaltime = totaltime;
        this.calls = calls;
    }

    @Override
    public String toString() {
        return "<Entry: " + code + ", " + callcount + ", " + reccallcount + ", " + inlinetime + ", " + totaltime + ", " +
                calls + ">";
    }
}
