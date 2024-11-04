package hownet.howNet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

public class How_Similarity {

    public Map<String, SememeElement> getSememetable() {
        return new HashMap<>();
    }

    public Map<String, GlossaryElement> getGlossarytable() {
        return new HashMap<>();
    }

    public Set<String> getVocab() {
        return new HashSet<>();
    }

    public SememeElement getSememeByID(String id) {
        return new SememeElement();
    }

    public SememeElement getSememeByZh(String zh) {
        return new SememeElement();
    }

    public List<GlossaryElement> getGlossary(String word) {
        return List.of(new GlossaryElement());
    }

    public double calcSememeSim(String sememe1, String sememe2) {
        return 0.0;
    }

    public double calc(String word1, String word2) {
        return 0.0;
    }
}
