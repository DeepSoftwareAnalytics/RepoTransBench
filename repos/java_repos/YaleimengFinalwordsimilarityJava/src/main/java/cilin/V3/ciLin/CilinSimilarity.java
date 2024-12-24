package cilin.V3.ciLin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CilinSimilarity {

    public Map<String, String> getCodeWord() {
        return new HashMap<>();
    }

    public Map<String, String> getWordCode() {
        return new HashMap<>();
    }

    public Set<String> getVocab() {
        return new HashSet<>();
    }

    public Map<String, String> getMydict() {
        return new HashMap<>();
    }

    public String getCommonStr(String str1, String str2) {
        return "";
    }

    public double InfoContent(String concept) {
        return 0.0;
    }

    public double simByIC(String c1, String c2) {
        return 0.0;
    }

    public double sim2018(String w1, String w2) {
        return 0.0;
    }
}
