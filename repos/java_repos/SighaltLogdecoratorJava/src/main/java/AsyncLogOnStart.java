import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncLogOnStart extends AsyncDecoratorMixin {
    private final LogOnStart logOnStart;

    public AsyncLogOnStart(Level logLevel, String message, Logger logger, String callableFormatVariable) {
        logOnStart = new LogOnStart(logLevel, message, logger, callableFormatVariable);
    }

    @Override
    public CompletableFuture<Object> executeAsync(Method fn, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logOnStart.execute(fn, args);
                return null;  // Return null to fulfill the CompletableFuture<Object> contract
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

