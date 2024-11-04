import java.util.regex.Pattern;

public class Validators {
    public static class Match {
        private Pattern pattern;

        public Match(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        public String validate(String value) throws Invalid {
            if (!pattern.matcher(value).matches()) {
                throw new Invalid("does not match regular expression " + pattern.pattern());
            }
            return value;
        }
    }

    public static class Range {
        private int min;
        private int max;

        public Range(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int validate(int value) throws Invalid {
            if (value < min || value > max) {
                throw new Invalid("value out of range");
            }
            return value;
        }
    }

    // Other validators can be added similarly
}

