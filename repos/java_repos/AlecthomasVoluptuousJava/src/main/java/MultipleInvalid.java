import java.util.ArrayList;
import java.util.List;

public class MultipleInvalid extends Invalid {
    private List<Invalid> errors;

    public MultipleInvalid(List<Invalid> errors) {
        super(errors.get(0).getMessage());
        this.errors = new ArrayList<>(errors);
    }

    public List<Invalid> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return errors.toString();
    }
}
