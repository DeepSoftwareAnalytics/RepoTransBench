import java.util.Collections;
import java.util.Set;

public class UnificationTerm {
    private String term;

    public UnificationTerm(String term) {
        this.term = term;
    }

    public Set<Variable> freeVariables() {
        return Collections.emptySet();
    }

    public UnificationTerm replace(UnificationTerm oldTerm, UnificationTerm newTerm) {
        if (this.equals(oldTerm)) {
            return newTerm;
        }
        return this;
    }

    public boolean occurs(UnificationTerm term) {
        return this.equals(term);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UnificationTerm that = (UnificationTerm) obj;
        return term.equals(that.term);
    }

    @Override
    public int hashCode() {
        return term.hashCode();
    }
}
