public class Progress {

    private int width;
    private double currentValue = 0;
    private Colorize c = new Colorize();

    public Progress(int width) {
        this.width = width;
    }

    public String update(double currentValue) {
        this.currentValue = currentValue;

        return String.format("[%s%s] %s",
                c.green("|".repeat(this.getBarLength())),
                "-".repeat(this.width - this.getBarLength()),
                c.grey(String.format("%d%%", this.getPercent()))
        );
    }

    public int getPercent() {
        return (int) (this.currentValue * 100);
    }

    public int getBarLength() {
        return (int) Math.ceil((double) this.width * this.getPercent() / 100);
    }
}
