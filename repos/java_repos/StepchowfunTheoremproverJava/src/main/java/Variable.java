import java.util.Collections;
import java.util.Set;

public class Variable {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public Set<Variable> freeVariables() {
        return Collections.singleton(this);
    }

    public Variable replace(Variable oldVar, Variable newVar) {
        if (this.equals(oldVar)) {
            return newVar;
        }
        return this;
    }

    public boolean occurs(UnificationTerm term) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Variable variable = (Variable) obj;
        return name.equals(variable.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
