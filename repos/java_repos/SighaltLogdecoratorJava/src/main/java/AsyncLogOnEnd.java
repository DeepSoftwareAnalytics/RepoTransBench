import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncLogOnEnd extends AsyncDecoratorMixin {
    private final LogOnEnd logOnEnd;

    public AsyncLogOnEnd(Level logLevel, String message, Logger logger, String callableFormatVariable, String resultFormatVariable) {
        logOnEnd = new LogOnEnd(logLevel, message, logger, callableFormatVariable, resultFormatVariable);
    }

    @Override
    public CompletableFuture<Object> executeAsync(Method fn, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return logOnEnd.execute(fn, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
