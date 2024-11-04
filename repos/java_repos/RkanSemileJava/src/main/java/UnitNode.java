import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class UnitNode {
    private Position position;
    private double elapsedTime;
    private String message;
    private List<UnitNode> children;
    private boolean finalized;
    private int serialNo;

    public UnitNode(Position position, double elapsedTime, String message) {
        this.position = position;
        this.elapsedTime = elapsedTime;
        this.message = message;
        this.children = new LinkedList<>();
        this.finalized = false;
        this.serialNo = -1;
    }

    public void addRow(ProfRow row, int serialNo) {
        // The logic to add a ProfRow to the UnitNode
        this.serialNo = serialNo;
        // Simplified logic for illustrative purposes
        UnitNode child = new UnitNode(row.getPositions().get(0), row.getCurrentTime(), row.getMessage());
        this.children.add(child);
    }

    public void finalize() {
        this.finalized = true;
    }

    public String traverse() {
        StringBuilder builder = new StringBuilder();
        traverseHelper(builder, "");
        return builder.toString();
    }

    private void traverseHelper(StringBuilder builder, String indent) {
        builder.append(indent).append(position).append(" ").append(elapsedTime).append(" ").append(message).append("\n");
        for (UnitNode child : children) {
            child.traverseHelper(builder, indent + "  ");
        }
    }

    public void buildView(BufferedWriter fd, int maxBranch, boolean isDot, boolean isRoot) throws IOException {
        // Simplified logic for illustration
        fd.write(this.toString());
        fd.newLine();
        for (UnitNode child : children) {
            child.buildView(fd, maxBranch, isDot, false);
        }
    }
}

