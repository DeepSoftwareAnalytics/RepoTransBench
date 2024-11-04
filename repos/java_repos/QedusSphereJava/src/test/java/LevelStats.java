public class LevelStats {
    private int count;
    private double minArea;
    private double maxArea;
    private double avgArea;
    private double minWidth;
    private double maxWidth;
    private double avgWidth;
    private double minEdge;
    private double maxEdge;
    private double avgEdge;
    private double maxEdgeAspect;
    private double minDiag;
    private double maxDiag;
    private double avgDiag;
    private double maxDiagAspect;
    private double minAngleSpan;
    private double maxAngleSpan;
    private double avgAngleSpan;
    private double minApproxRatio;
    private double maxApproxRatio;

    public LevelStats() {
        this.count = 0;
        this.minArea = 100;
        this.maxArea = 0;
        this.avgArea = 0;
        this.minWidth = 100;
        this.maxWidth = 0;
        this.avgWidth = 0;
        this.minEdge = 100;
        this.maxEdge = 0;
        this.avgEdge = 0;
        this.maxEdgeAspect = 0;
        this.minDiag = 100;
        this.maxDiag = 0;
        this.avgDiag = 0;
        this.maxDiagAspect = 0;
        this.minAngleSpan = 100;
        this.maxAngleSpan = 0;
        this.avgAngleSpan = 0;
        this.minApproxRatio = 100;
        this.maxApproxRatio = 0;
    }

    // Getters and Setters for each field

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getMinArea() {
        return minArea;
    }

    public void setMinArea(double minArea) {
        this.minArea = minArea;
    }

    public double getMaxArea() {
        return maxArea;
    }

    public void setMaxArea(double maxArea) {
        this.maxArea = maxArea;
    }

    public double getAvgArea() {
        return avgArea;
    }

    public void setAvgArea(double avgArea) {
        this.avgArea = avgArea;
    }

    public double getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(double minWidth) {
        this.minWidth = minWidth;
    }

    public double getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
    }

    public double getAvgWidth() {
        return avgWidth;
    }

    public void setAvgWidth(double avgWidth) {
        this.avgWidth = avgWidth;
    }

    public double getMinEdge() {
        return minEdge;
    }

    public void setMinEdge(double minEdge) {
        this.minEdge = minEdge;
    }

    public double getMaxEdge() {
        return maxEdge;
    }

    public void setMaxEdge(double maxEdge) {
        this.maxEdge = maxEdge;
    }

    public double getAvgEdge() {
        return avgEdge;
    }

    public void setAvgEdge(double avgEdge) {
        this.avgEdge = avgEdge;
    }

    public double getMaxEdgeAspect() {
        return maxEdgeAspect;
    }

    public void setMaxEdgeAspect(double maxEdgeAspect) {
        this.maxEdgeAspect = maxEdgeAspect;
    }

    public double getMinDiag() {
        return minDiag;
    }

    public void setMinDiag(double minDiag) {
        this.minDiag = minDiag;
    }

    public double getMaxDiag() {
        return maxDiag;
    }

    public void setMaxDiag(double maxDiag) {
        this.maxDiag = maxDiag;
    }

    public double getAvgDiag() {
        return avgDiag;
    }

    public void setAvgDiag(double avgDiag) {
        this.avgDiag = avgDiag;
    }

    public double getMaxDiagAspect() {
        return maxDiagAspect;
    }

    public void setMaxDiagAspect(double maxDiagAspect) {
        this.maxDiagAspect = maxDiagAspect;
    }

    public double getMinAngleSpan() {
        return minAngleSpan;
    }

    public void setMinAngleSpan(double minAngleSpan) {
        this.minAngleSpan = minAngleSpan;
    }

    public double getMaxAngleSpan() {
        return maxAngleSpan;
    }

    public void setMaxAngleSpan(double maxAngleSpan) {
        this.maxAngleSpan = maxAngleSpan;
    }

    public double getAvgAngleSpan() {
        return avgAngleSpan;
    }

    public void setAvgAngleSpan(double avgAngleSpan) {
        this.avgAngleSpan = avgAngleSpan;
    }

    public double getMinApproxRatio() {
        return minApproxRatio;
    }

    public void setMinApproxRatio(double minApproxRatio) {
        this.minApproxRatio = minApproxRatio;
    }

    public double getMaxApproxRatio() {
        return maxApproxRatio;
    }

    public void setMaxApproxRatio(double maxApproxRatio) {
        this.maxApproxRatio = maxApproxRatio;
    }
}
