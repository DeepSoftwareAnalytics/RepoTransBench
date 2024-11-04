public class ParserError extends Exception {
    private String buf;
    private int pos;
    private String reason;

    public ParserError(String buf, int pos, String reason) {
        super(String.format("%s (at pos=%d)", reason, pos));
        this.buf = buf;
        this.pos = pos;
        this.reason = reason;
    }

    public String name() {
        return this.getClass().getSimpleName();
    }

    public String explain() {
        return this.reason;
    }
    
    public int getPos() {
        return this.pos;
    }
}
