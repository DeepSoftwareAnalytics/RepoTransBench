public class SlidingWindowFeature {
    private double[][] data;
    private SlidingWindow window;

    public SlidingWindowFeature(double[][] data, SlidingWindow window) {
        this.data = data;
        this.window = window;
    }

    public double[][] crop(Segment segment, String mode) {
        return crop(segment, mode, -1);
    }

    public double[][] crop(Segment segment, String mode, double fixedDuration) {
        int startIndex = (int) ((segment.getStart() - window.getStart()) / window.getStep());
        int endIndex = (int) ((segment.getEnd() - window.getStart()) / window.getStep());

        if (mode.equals("strict")) {
            startIndex = Math.max(startIndex, 0);
            endIndex = Math.min(endIndex, data[0].length - 1);
        } else if (mode.equals("center")) {
            startIndex = Math.max(startIndex - (int) (fixedDuration / window.getStep() / 2), 0);
            endIndex = startIndex + (int) (fixedDuration / window.getStep());
        }

        double[][] result = new double[data.length][endIndex - startIndex + 1];
        for (int i = 0; i < data.length; i++) {
            System.arraycopy(data[i], startIndex, result[i], 0, endIndex - startIndex + 1);
        }
        return result;
    }
}
