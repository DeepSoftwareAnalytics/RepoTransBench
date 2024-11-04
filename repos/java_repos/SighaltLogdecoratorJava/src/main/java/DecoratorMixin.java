import java.lang.reflect.Method;

public abstract class DecoratorMixin {
    public abstract Object execute(Method fn, Object... args) throws Exception;

    public Object call(Object instance, Method fn) throws Exception {
        return execute(fn, instance);
    }
}
