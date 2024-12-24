import java.util.ArrayList;
import java.util.List;

public class Table {
    public final List<String[]> cells;
    public final List<Integer> widths;

    public Table(List<String[]> cells) {
        this.cells = cells;
        this.widths = calculateWidths(cells);
    }

    public String markdown(List<Integer> centerAlignedColumns, List<Integer> rightAlignedColumns, boolean noHeaderRow) {
        if (cells.size() == 0) {
            return "";
        }

        StringBuilder markdown = new StringBuilder();
        String headerSeparator = getHeaderSeparator(centerAlignedColumns, rightAlignedColumns);

        for (int i = 0; i < cells.size(); i++) {
            if (i == 1 && !noHeaderRow) {
                markdown.append("\n").append(headerSeparator);
            }
            markdown.append(formatRow(cells.get(i)).replaceFirst("\\|", "").trim()).append("\n");
        }
        return markdown.toString();
    }

    private List<Integer> calculateWidths(List<String[]> cells) {
        List<Integer> columnWidths = new ArrayList<>();
        for (String cell : cells.get(0)) {
            columnWidths.add(cell.length());
        }
        for (String[] row : cells) {
            for (int i = 0; i < row.length; i++) {
                columnWidths.set(i, Math.max(columnWidths.get(i), row[i].length()));
            }
        }
        return columnWidths;
    }

    private String formatRow(String[] row) {
        StringBuilder formattedRow = new StringBuilder("| ");
        for (int i = 0; i < row.length; i++) {
            formattedRow.append(row[i].trim()).append(" ".repeat(Math.max(0, widths.get(i) - row[i].length()))).append(" | ");
        }
        return formattedRow.toString();
    }

    private String getHeaderSeparator(List<Integer> centerAlignedColumns, List<Integer> rightAlignedColumns) {
        StringBuilder separator = new StringBuilder("|");

        for (int i = 0; i < widths.size(); i++) {
            String cellSeparator;

            if (rightAlignedColumns != null && rightAlignedColumns.contains(i)) {
                cellSeparator = " ".repeat(widths.get(i) - 1) + ":";
            } else if (centerAlignedColumns != null && centerAlignedColumns.contains(i)) {
                cellSeparator = ":" + " ".repeat(widths.get(i) - 2) + ":";
            } else {
                cellSeparator = "-".repeat(widths.get(i));
            }

            separator.append(" ").append(cellSeparator).append(" |");
        }
        return separator.toString();
    }

    public static Table parseCSV(List<String[]> rows, List<Integer> columns) {
        List<String[]> filteredRows = new ArrayList<>();

        if (columns == null || columns.isEmpty()) {
            filteredRows.addAll(rows);
        } else {
            for (String[] row : rows) {
                List<String> filteredRow = new ArrayList<>();
                for (int column : columns) {
                    if (column >= 0 && column < row.length) {
                        filteredRow.add(row[column]);
                    }
                }
                filteredRows.add(filteredRow.toArray(new String[0]));
            }
        }

        return new Table(filteredRows);
    }
}
