import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncLogOnError extends AsyncDecoratorMixin {
    private final LogOnError logOnError;

    public AsyncLogOnError(Level logLevel, String message, Logger logger, String callableFormatVariable,
                           Class<? extends Exception>[] onExceptions, boolean reraise, String exceptionFormatVariable) {
        logOnError = new LogOnError(logLevel, message, logger, callableFormatVariable, onExceptions, reraise, exceptionFormatVariable);
    }

    @Override
    public CompletableFuture<Object> executeAsync(Method fn, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return logOnError.execute(fn, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
