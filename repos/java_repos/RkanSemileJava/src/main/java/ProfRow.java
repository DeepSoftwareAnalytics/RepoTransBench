import java.util.List;

public class ProfRow {
    private List<Position> positions;
    private double currentTime;
    private String message;

    public ProfRow(List<Position> positions, double currentTime, String message) {
        this.positions = positions;
        this.currentTime = currentTime;
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder posStr = new StringBuilder();
        for (Position pos : positions) {
            posStr.append(pos.toString());
        }
        if (posStr.length() == 0) {
            posStr = new StringBuilder("EMPTY_POS");
        }
        return posStr + " " + currentTime + " " + message;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

