import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Formatter {
    private static final String TEMPLATE_VARIABLE_OPENING_TAG = "___TEMPLATE_VARIABLE_OPENING_TAG___";
    private static final String TEMPLATE_VARIABLE_CLOSING_TAG = "___TEMPLATE_VARIABLE_CLOSING_TAG___";

    private static final String TEMPLATE_BRACKET_OPENING_TAG = "___TEMPLATE_BRACKET_OPENING_TAG___";
    private static final String TEMPLATE_BRACKET_CLOSING_TAG = "___TEMPLATE_BRACKET_CLOSING_TAG___";

    private final Logger logger;
    private final FormatterOptions options;

    public Formatter() {
        this(new FormatterOptions(), Logger.getLogger(Formatter.class.getName()));
    }

    public Formatter(FormatterOptions options) {
        this(options, Logger.getLogger(Formatter.class.getName()));
    }

    public Formatter(FormatterOptions options, Logger logger) {
        this.options = options;
        this.logger = logger;
    }

    public String formatString(String contents) {
        String[] lines = contents.split("\\r?\\n");
        lines = applyBracketTemplateTags(lines);
        lines = cleanLines(lines);
        lines = joinOpeningBracket(lines);
        lines = performIndentation(lines);

        String text = String.join("\n", lines);
        text = stripBracketTemplateTags(text);

        text = text.replaceAll("\\n{3,}", "\n\n\n");
        text = text.replaceAll("^\\n", "");
        text = text.replaceAll("\\n$", "");

        return text + "\n";
    }

    public String getFormattedStringFromFile(Path filePath) throws IOException {
        String originalFileContent = new String(Files.readAllBytes(filePath));
        return formatString(originalFileContent);
    }

    public void formatFile(Path filePath, Path originalBackupFilePath) throws IOException {
        String originalFileContent = new String(Files.readAllBytes(filePath));
        String formattedContent = formatString(originalFileContent);

        Files.write(filePath, formattedContent.getBytes());

        logger.info("Formatted content written to original file.");

        if (originalBackupFilePath != null) {
            Files.write(originalBackupFilePath, originalFileContent.getBytes());
            logger.info(String.format("Original content saved to '%s'.", originalBackupFilePath));
        }
    }

    private static String stripLine(String singleLine) {
        singleLine = singleLine.trim();
        if (singleLine.startsWith("#")) {
            return singleLine;
        }

        boolean withinQuotes = false;
        StringBuilder parts = new StringBuilder();
        for (String part : singleLine.split("\"")) {
            if (withinQuotes) {
                parts.append(part);
            } else {
                parts.append(part.replaceAll("[\\s]+", " "));
            }
            parts.append("\"");
            withinQuotes = !withinQuotes;
        }
        singleLine = parts.toString();
        return singleLine.substring(0, singleLine.length() - 1);
    }

    private static int[] countMultiSemicolon(String singleLine) {
        singleLine = singleLine.trim();
        if (singleLine.startsWith("#")) {
            return new int[]{0, 0};
        }

        boolean withinQuotes = false;
        int q = 0;
        int c = 0;
        for (String part : singleLine.split("\"")) {
            if (withinQuotes) {
                q = 1;
            } else {
                c += part.length() - part.replace(";", "").length();
            }
            withinQuotes = !withinQuotes;
        }
        return new int[]{q, c};
    }

    private static String multiSemicolon(String singleLine) {
        singleLine = singleLine.trim();
        if (singleLine.startsWith("#")) {
            return singleLine;
        }

        boolean withinQuotes = false;
        StringBuilder parts = new StringBuilder();
        for (String part : singleLine.split("\"")) {
            if (withinQuotes) {
                parts.append(part);
            } else {
                parts.append(part.replace(";", ";\n"));
            }
            parts.append("\"");
            withinQuotes = !withinQuotes;
        }
        singleLine = parts.toString();
        return singleLine.substring(0, singleLine.length() - 1);
    }

    // Change the access modifier to package-private
    String applyVariableTemplateTags(String line) {
        return line.replaceAll("\\$\\{\\s*(\\w+)\\s*}", TEMPLATE_VARIABLE_OPENING_TAG + "$1" + TEMPLATE_VARIABLE_CLOSING_TAG);
    }

    String stripVariableTemplateTags(String line) {
        return line.replaceAll(TEMPLATE_VARIABLE_OPENING_TAG + "\\s*(\\w+)\\s*" + TEMPLATE_VARIABLE_CLOSING_TAG, "\\${$1}");
    }

    String[] applyBracketTemplateTags(String[] lines) {
        List<String> formattedLines = new ArrayList<>();

        for (String line : lines) {
            StringBuilder formattedLine = new StringBuilder();
            boolean inQuotes = false;
            char lastChar = '\0';

            if (line.startsWith("#")) {
                formattedLine.append(line);
            } else {
                for (char ch : line.toCharArray()) {
                    if ((ch == '\'' || ch == '\"') && lastChar != '\\') {
                        inQuotes = !inQuotes;
                    }

                    if (inQuotes) {
                        if (ch == '{') {
                            formattedLine.append(TEMPLATE_BRACKET_OPENING_TAG);
                        } else if (ch == '}') {
                            formattedLine.append(TEMPLATE_BRACKET_CLOSING_TAG);
                        } else {
                            formattedLine.append(ch);
                        }
                    } else {
                        formattedLine.append(ch);
                    }

                    lastChar = ch;
                }
            }

            formattedLines.add(formattedLine.toString());
        }

        return formattedLines.toArray(new String[0]);
    }

    String stripBracketTemplateTags(String content) {
        content = content.replace(TEMPLATE_BRACKET_OPENING_TAG, "{");
        content = content.replace(TEMPLATE_BRACKET_CLOSING_TAG, "}");
        return content;
    }

    // Change the access modifier to package-private
    String[] cleanLines(String[] origLines) {
        List<String> cleanedLines = new ArrayList<>();
        for (String line : origLines) {
            line = stripLine(line);
            line = applyVariableTemplateTags(line);
            if (line.isEmpty()) {
                cleanedLines.add("");
            } else if (line.equals("};")) {
                cleanedLines.add("}");
            } else if (line.startsWith("#")) {
                cleanedLines.add(stripVariableTemplateTags(line));
            } else {
                int[] qc = countMultiSemicolon(line);
                if (qc[0] == 1 && qc[1] > 1) {
                    String ml = multiSemicolon(line);
                    cleanedLines.addAll(List.of(cleanLines(ml.split("\\r?\\n"))));
                } else {
                    for (String ln : line.split("(?<=\\})|(?=\\})|(?<=\\{)|(?=\\{)")) {
                        cleanedLines.add(stripVariableTemplateTags(ln).trim());
                    }
                }
            }
        }
        return cleanedLines.toArray(new String[0]);
    }

    // Change the access modifier to package-private
    String[] joinOpeningBracket(String[] lines) {
        List<String> modifiedLines = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0 && lines[i].equals("{")) {
                modifiedLines.set(modifiedLines.size() - 1, modifiedLines.get(modifiedLines.size() - 1) + " {");
            } else {
                modifiedLines.add(lines[i]);
            }
        }
        return modifiedLines.toArray(new String[0]);
    }

    // Change the access modifier to package-private
    String[] performIndentation(String[] lines) {
        List<String> indentedLines = new ArrayList<>();
        int currentIndent = 0;
        String indentationStr = " ".repeat(this.options.indentation);

        for (String line : lines) {
            if (!line.startsWith("#") && line.endsWith("}") && currentIndent > 0) {
                currentIndent--;
            }

            if (!line.isEmpty()) {
                indentedLines.add(indentationStr.repeat(currentIndent) + line);
            } else {
                indentedLines.add("");
            }

            if (!line.startsWith("#") && line.endsWith("{")) {
                currentIndent++;
            }
        }

        return indentedLines.toArray(new String[0]);
    }
}
