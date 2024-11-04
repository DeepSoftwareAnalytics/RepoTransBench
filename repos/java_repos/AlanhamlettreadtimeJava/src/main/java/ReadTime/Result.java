package ReadTime;

import java.time.Duration;

public class Result {

    private final Duration delta;
    private final int wpm;

    public Result(long seconds, int wpm) {
        this.delta = Duration.ofSeconds(seconds);
        this.wpm = wpm;
    }

    @Override
    public String toString() {
        return getText() + " read";
    }

    public long getSeconds() {
        return delta.getSeconds();
    }

    public long getMinutes() {
        long minutes = (long) Math.ceil(getSeconds() / 60.0);
        return Math.max(1, minutes); // Minimum of 1 minute read time
    }

    public String getText() {
        return getMinutes() + " min";
    }

    public int getWpm() {
        return wpm;
    }

    public Result add(Result other) {
        long combinedSeconds = this.getSeconds() + other.getSeconds();
        return new Result(combinedSeconds, this.wpm);
    }

    public Result combine(Result other) {
        return this.add(other);
    }

    public Result plus(Result other) {
        return this.add(other);
    }

    public Result minus(Result other) {
        long resultSeconds = this.getSeconds() - other.getSeconds();
        resultSeconds = Math.max(0, resultSeconds); // Ensure non-negative
        return new Result(resultSeconds, this.wpm);
    }
}
