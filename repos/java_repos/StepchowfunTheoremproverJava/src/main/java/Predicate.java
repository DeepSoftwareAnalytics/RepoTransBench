import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
public class Predicate {
    private String name;
    private List<Variable> arguments;

    public Predicate(String name, List<Variable> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public Set<Variable> freeVariables() {
        return new HashSet<>(arguments);
    }

    public Predicate replace(Variable oldVar, Variable newVar) {
        List<Variable> newArgs = arguments.stream()
                .map(arg -> arg.equals(oldVar) ? newVar : arg)
                .collect(Collectors.toList());
        return new Predicate(name, newArgs);
    }

    public boolean occurs(UnificationTerm term) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Predicate predicate = (Predicate) obj;
        return name.equals(predicate.name) && arguments.equals(predicate.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, arguments);
    }

    @Override
    public String toString() {
        return name + "(" + arguments.stream().map(Variable::toString).collect(Collectors.joining(", ")) + ")";
    }
}
