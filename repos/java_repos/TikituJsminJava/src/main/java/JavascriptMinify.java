import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class JavascriptMinify {

    private BufferedReader ins;
    private Writer outs;
    private String quoteChars = "'\"";
    private String newlineendStrings;
    private String newlinestartStrings;

    public JavascriptMinify(Reader instream, Writer outstream, String quoteChars) {
        this.ins = new BufferedReader(instream);
        this.outs = outstream;
        this.quoteChars = quoteChars;
        this.newlinestartStrings = "{[(+-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_$\\" + quoteChars;
        this.newlineendStrings = "}])+-/" + quoteChars + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_$\\";
    }

    public void minify() throws IOException {
        boolean isReturn = false;
        StringBuilder returnBuf = new StringBuilder();

        String spaceStrings = "abcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_$\\";
        String starters = "{[(+-";
        String enders = "}])+-/" + quoteChars;

        boolean doNewline = false;
        boolean doSpace = false;
        int escapeSlashCount = 0;
        String inQuote = "";
        StringBuilder quoteBuf = new StringBuilder();

        int previous = ';';
        int previousNonSpace = ';';

        int next1 = ins.read();

        while (next1 != -1) {
            int next2 = ins.read();
            if (!inQuote.isEmpty()) {
                quoteBuf.append((char) next1);

                if (next1 == inQuote.charAt(0)) {
                    int numslashes = 0;
                    for (int i = quoteBuf.length() - 2; i >= 0; i--) {
                        if (quoteBuf.charAt(i) != '\\')
                            break;
                        else
                            numslashes++;
                    }
                    if (numslashes % 2 == 0) {
                        inQuote = "";
                        outs.write(quoteBuf.toString());
                    }
                }
            } else if (next1 == '\r' || next1 == '\n') {
                int[] result = newline(previousNonSpace, next2, doNewline);
                next2 = result[0];
                doNewline = result[1] == 1;
            } else if (next1 < '!') {
                if ((spaceStrings.indexOf(previousNonSpace) != -1 || previousNonSpace > '~') &&
                        (spaceStrings.indexOf(next2) != -1 || next2 > '~')) {
                    doSpace = true;
                } else if ((previousNonSpace == '-' || previousNonSpace == '+') && next2 == previousNonSpace) {
                    doSpace = true;
                } else if (isReturn && next2 == '/') {
                    outs.write(' ');
                }
            } else if (next1 == '/') {
                if (doSpace) {
                    outs.write(' ');
                }
                if (next2 == '/') {
                    next2 = lineComment(next1, next2);
                    next1 = '\n';
                    int[] result = newline(previousNonSpace, next2, doNewline);
                    next2 = result[0];
                    doNewline = result[1] == 1;
                } else if (next2 == '*') {
                    blockComment(next1, next2);
                    next2 = ins.read();
                    if (spaceStrings.indexOf(previousNonSpace) != -1) {
                        doSpace = true;
                    }
                    next1 = previous;
                } else {
                    if ("{(,=:[?!&|;".indexOf(previousNonSpace) != -1 || isReturn) {
                        regexLiteral(next1, next2);
                        next2 = ins.read();
                    } else {
                        outs.write('/');
                    }
                }
            } else {
                if (doNewline) {
                    outs.write('\n');
                    doNewline = false;
                    doSpace = false;
                }
                if (doSpace) {
                    doSpace = false;
                    outs.write(' ');
                }

                outs.write(next1);
                if (quoteChars.indexOf((char) next1) != -1) {
                    inQuote = "" + (char) next1;
                    quoteBuf = new StringBuilder();
                }
            }

            if (next1 >= '!') {
                previousNonSpace = next1;
            }

            if (next1 == '\\') {
                escapeSlashCount += 1;
            } else {
                escapeSlashCount = 0;
            }

            previous = next1;
            next1 = next2;
        }
    }

    private int lineComment(int next1, int next2) throws IOException {
        assert next1 == '/' && next2 == '/';

        int next = ins.read();

        while (next != -1 && next != '\r' && next != '\n') {
            next = ins.read();
        }
        while (next != -1 && (next == '\r' || next == '\n')) {
            next = ins.read();
        }
        return next;
    }

    private void blockComment(int next1, int next2) throws IOException {
        assert next1 == '/' && next2 == '*';

        int next = ins.read();
        next = ins.read();

        String commentBuffer = "/*";

        while (next1 != '*' || next2 != '/') {
            commentBuffer += (char) next1;
            next1 = next2;
            next2 = ins.read();
        }

        if (commentBuffer.startsWith("/*!")) {
            outs.write(commentBuffer);
            outs.write("*/\n");
        }
    }

    private int[] newline(int previousNonSpace, int next2, boolean doNewline) throws IOException {
        if ((previousNonSpace != 0) && (newlineendStrings.indexOf((char) previousNonSpace) != -1 || previousNonSpace > '~')) {
            while (next2 < '!') {
                next2 = ins.read();
                if (next2 == -1) break;
            }

            if ((newlinestartStrings.indexOf((char) next2) != -1 || next2 > '~' || next2 == '/')) {
                doNewline = true;
            }
        }
        return new int[]{next2, doNewline ? 1 : 0};
    }

    public void regexLiteral(int next1, int next2) throws IOException {
        assert next1 == '/';

        boolean inCharClass = false;
        outs.write('/');

        int next = next2;
        while (next != -1 && (next != '/' || inCharClass)) {
            outs.write(next);
            if (next == '\\') {
                outs.write(ins.read());
            } else if (next == '[') {
                outs.write(ins.read());
                inCharClass = true;
            } else if (next == ']') {
                inCharClass = false;
            }
            next = ins.read();
        }
        outs.write('/');
    }
}

