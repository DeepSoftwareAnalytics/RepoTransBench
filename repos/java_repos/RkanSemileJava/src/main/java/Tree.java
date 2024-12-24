import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class Tree {
    private double currentTime;
    private double totalTime = 0;
    private final UnitNode root;
    private int serialNo = 1;

    public Tree(double initTime) {
        this.currentTime = initTime;
        this.root = new UnitNode(new Position("root", "rootFunc", 0), 0, "root");
    }

    public void addRow(ProfRow row) {
        double elapsedTime = row.getCurrentTime() - this.currentTime - ProfileParser.getReducerTime(); // Use getReducerTime method
        if (elapsedTime <= 0) elapsedTime = 0.0001;
        this.totalTime += elapsedTime;
        this.currentTime = row.getCurrentTime();
        this.root.addRow(new ProfRow(row.getPositions(), elapsedTime, row.getMessage()), this.serialNo++);
    }

    public void finalizeTree() {
        this.root.finalize();
    }

    public String traverse() {
        return this.root.traverse();
    }

    public void buildView(BufferedWriter fd, int maxBranch, boolean isDot) throws IOException {
        this.root.buildView(fd, maxBranch, isDot, false);
    }
}

