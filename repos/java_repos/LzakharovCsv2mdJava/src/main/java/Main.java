import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ArgsParser argsParser = ArgsParser.parseArgs(args);
        List<Table> tables = new ArrayList<>();

        for (String file : argsParser.getFiles()) {
            try (Reader reader = new FileReader(file)) {
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(argsParser.getDelimiter())
                        .withQuote(argsParser.getQuoteChar()));
                List<String[]> rows = new ArrayList<>();
                for (CSVRecord csvRecord : csvParser.getRecords()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 0; i < csvRecord.size(); i++) {
                        row.add(csvRecord.get(i));
                    }
                    rows.add(row.toArray(new String[0]));
                }
                tables.add(Table.parseCSV(rows, argsParser.getColumns()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Table table : tables) {
            System.out.println(table.markdown(argsParser.getCenterAlignedColumns(), argsParser.getRightAlignedColumns(), argsParser.isNoHeaderRow()));
        }
    }
}
