import java.io.*;
import java.util.*;
import org.apache.commons.csv.*;

public class DataGenerator implements Iterable<DataGenerator.Data> {

    private final List<Data> data;
    private final int hashSize;
    private final String hashSalt;

    public DataGenerator(String filePath, int hashSize, String hashSalt) throws IOException {
        this.data = new ArrayList<>();
        this.hashSize = hashSize;
        this.hashSalt = hashSalt;

        try (Reader in = new FileReader(filePath)) {
            CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            for (CSVRecord record : parser) {
                int t = (int) record.getRecordNumber();
                String ID = record.get("id");
                String hour = record.get("hour");
                int date = Integer.parseInt(hour.substring(4, 6));
                hour = hour.substring(6);

                double y = record.isSet("click") && "1".equals(record.get("click")) ? 1.0 : 0.0;
                List<Integer> x = new ArrayList<>();

                for (String field : record) {
                    int hash = Math.abs((hashSalt + field).hashCode()) % hashSize + 1;
                    x.add(hash);
                }

                data.add(new Data(t, date, ID, x, y));
            }
        }
    }

    @Override
    public Iterator<Data> iterator() {
        return data.iterator();
    }

    public static class Data {
        public final int t, date;
        public final String ID;
        public final List<Integer> x;
        public final double y;

        public Data(int t, int date, String ID, List<Integer> x, double y) {
            this.t = t;
            this.date = date;
            this.ID = ID;
            this.x = x;
            this.y = y;
        }
    }
}
