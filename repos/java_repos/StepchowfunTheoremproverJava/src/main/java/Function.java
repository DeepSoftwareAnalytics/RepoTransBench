import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
public class Function {
    private String name;
    private List<Variable> arguments;

    public Function(String name, List<Variable> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public Set<Variable> freeVariables() {
        return new HashSet<>(arguments);
    }

    public Function replace(Variable oldVar, Variable newVar) {
        List<Variable> newArgs = arguments.stream()
                .map(arg -> arg.equals(oldVar) ? newVar : arg)
                .collect(Collectors.toList());
        return new Function(name, newArgs);
    }

    public boolean occurs(UnificationTerm term) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Function function = (Function) obj;
        return name.equals(function.name) && arguments.equals(function.arguments);
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
