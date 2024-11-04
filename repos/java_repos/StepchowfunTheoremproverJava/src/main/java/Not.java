import java.util.Set;
import java.util.Objects;
public class Not {
    private Predicate formula;

    public Not(Predicate formula) {
        this.formula = formula;
    }

    public Set<Variable> freeVariables() {
        return formula.freeVariables();
    }

    public Not replace(Variable oldVar, Variable newVar) {
        return new Not(formula.replace(oldVar, newVar));
    }

    public boolean occurs(UnificationTerm term) {
        return formula.occurs(term);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Not not = (Not) obj;
        return formula.equals(not.formula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formula);
    }

    @Override
    public String toString() {
        return "¬" + formula.toString();
    }
}
