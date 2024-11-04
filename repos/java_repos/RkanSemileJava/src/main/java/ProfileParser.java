import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.charset.StandardCharsets;

public class ProfileParser implements Iterable<ProfRow> {
    private static double reducerTime = 0.0; // Removed `final` modifier to allow modification

    private List<ProfRow> rows;
    private double startTime;
    private Iterator<ProfRow> iterator;

    public ProfileParser(String path) throws IOException {
        this.rows = parse(path);
        this.iterator = this.rows.iterator();
    }

    @Override
    public Iterator<ProfRow> iterator() {
        return this.iterator;
    }

    private List<ProfRow> parse(String path) throws IOException {
        File file = new File(path);
        List<ProfRow> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("+{") || !line.contains("}+")) {
                    continue;
                }
                String positionsSegment = line.substring(line.indexOf("+{") + 2, line.indexOf("}+"));
                List<Position> positions = parsePositions(positionsSegment);
                String lastHalfSegment = line.substring(line.indexOf("}+") + 2);
                int posMessageBegin = lastHalfSegment.indexOf(" ");

                double currentTime = Double.parseDouble(lastHalfSegment.substring(0, posMessageBegin));
                String message = lastHalfSegment.substring(posMessageBegin).trim();
                rows.add(new ProfRow(positions, currentTime, message));
            }
        }

        computeReducedTime(rows);
        adjustTimes(rows);

        return rows;
    }

    private List<Position> parsePositions(String segment) {
        List<Position> positions = new ArrayList<>();
        if (!segment.isEmpty()) {
            String[] posArray = segment.split(",");
            for (String pos : posArray) {
                String[] items = pos.split(" ");
                positions.add(new Position(items[0], items[1], Integer.parseInt(items[2])));
            }
        }
        return positions;
    }

    private void computeReducedTime(List<ProfRow> rows) {
        if (!GlobalDef.useReducedTime()) {
            return;
        }

        List<Double> elapsedTimes = new ArrayList<>();
        for (int i = 0; i < rows.size() - 1; i++) {
            double elapsedTime = rows.get(i + 1).getCurrentTime() - rows.get(i).getCurrentTime();
            elapsedTimes.add(elapsedTime);
        }

        Collections.sort(elapsedTimes);
        if (!elapsedTimes.isEmpty()) {
            ProfileParser.reducerTime = elapsedTimes.get(elapsedTimes.size() / 2);
        }
    }

    private void adjustTimes(List<ProfRow> rows) {
        for (int i = 0; i < rows.size(); i++) {
            double currentTime = rows.get(i).getCurrentTime();
            if (i == 0) {
                this.startTime = currentTime;
            }
            if (i < rows.size() - 1) {
                rows.get(i).setCurrentTime(rows.get(i + 1).getCurrentTime());
            } else {
                rows.get(i).setCurrentTime(currentTime + 0.0001);
            }
        }
    }

    public double getStartTime() {
        return startTime;
    }

    public static double getReducerTime() {
        return reducerTime;
    }
}

