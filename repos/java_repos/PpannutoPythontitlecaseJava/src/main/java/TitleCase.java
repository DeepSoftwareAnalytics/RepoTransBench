// TitleCase.java 
import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

public class TitleCase {

    private static final String SMALL = "a|an|and|as|at|but|by|en|for|if|in|of|on|or|the|to|v\\.?|via|vs\\.?";
    private static final String PUNCT = "!“\"#$%&'’()*+,\\-–‒—―./:;<=>?@[\\\\]_`{|}~";

    private static final Pattern SMALL_WORDS = Pattern.compile("^(?i)" + SMALL + "$");
    private static final Pattern SMALL_FIRST = Pattern.compile("^(?i)([" + PUNCT + "]*)(?i)" + SMALL + "\\b");
    private static final Pattern SMALL_LAST = Pattern.compile("\\b(?i)" + SMALL + "[$" + PUNCT + "]?$");
    private static final Pattern SUBPHRASE = Pattern.compile("([:.;?!\\-–‒—―][ ])(?i)" + SMALL);
    private static final Pattern MAC_MC = Pattern.compile("^([Mm]c|MC)(\\w.+)");
    private static final Pattern MR_MRS_MS_DR = Pattern.compile("(?i)^((m((rs?)|s))|Dr)$");

    public String titleCase(String input) {
        return titleCase(input, null, true);
    }

    public String titleCase(String text, BiFunction<String, Map<String, Object>, String> callback, boolean smallFirstLast) {
        if (text == null || text.length() == 0) {
            return text;
        }

        boolean allCaps = text.equals(text.toUpperCase());

        String[] words = text.split("\\s");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (callback != null) {
                String newWord = callback.apply(word, Collections.singletonMap("all_caps", allCaps));
                if (newWord != null) {
                    result.append(newWord).append(" ");
                    continue;
                }
            }

            if (allCaps && word.matches("\\p{Upper}+")) {
                result.append(word).append(" ");
                continue;
            }

            Matcher matcher = MAC_MC.matcher(word);
            if (matcher.find()) {
                result.append(matcher.group(1).substring(0, 1).toUpperCase()).append(matcher.group(1).substring(1));
                result.append(titleCase(matcher.group(2)));
                result.append(" ");
                continue;
            }

            matcher = MR_MRS_MS_DR.matcher(word);
            if (matcher.find()) {
                result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
                continue;
            }

            if (SMALL_WORDS.matcher(word).matches()) {
                result.append(word.toLowerCase()).append(" ");
                continue;
            }

            if (word.contains("/") && !word.contains("//")) {
                String[] slashed = word.split("/");
                for (int i = 0; i < slashed.length; i++) {
                    slashed[i] = titleCase(slashed[i]);
                }
                result.append(String.join("/", slashed)).append(" ");
                continue;
            }

            if (word.contains("-")) {
                String[] hyphenated = word.split("-");
                for (int i = 0; i < hyphenated.length; i++) {
                    hyphenated[i] = titleCase(hyphenated[i]);
                }
                result.append(String.join("-", hyphenated)).append(" ");
                continue;
            }

            if (allCaps) {
                word = word.toLowerCase();
            }

            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }

        String resultStr = result.toString().trim();

        if (smallFirstLast) {
            resultStr = SMALL_FIRST.matcher(resultStr).replaceAll(m -> m.group(1) + m.group(2).toUpperCase());
            resultStr = SMALL_LAST.matcher(resultStr).replaceAll(m -> m.group(0).toUpperCase());
        }

        resultStr = SUBPHRASE.matcher(resultStr).replaceAll(m -> m.group(1) + m.group(2).toUpperCase());

        return resultStr;
    }

    public Function<String, String> createWordlistFilterFromFile(String filePath) {
        if (filePath == null) {
            return word -> null;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return word -> null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            Set<String> abbrevs = new HashSet<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    abbrevs.add(line.trim().toUpperCase());
                }
            }
            return word -> abbrevs.contains(word.toUpperCase()) ? word.toUpperCase() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return word -> null;
    }
}
