import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public abstract class AsyncDecoratorMixin extends DecoratorMixin {
    public abstract CompletableFuture<Object> executeAsync(Method fn, Object... args);

    @Override
    public Object execute(Method fn, Object... args) throws Exception {
        return executeAsync(fn, args);
    }
}
