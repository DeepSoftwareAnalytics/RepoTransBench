import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LogOnError extends LoggingDecorator {
    private Class<? extends Exception>[] onExceptions;
    private boolean reraise;
    private String exceptionFormatVariable;

    public LogOnError(Level logLevel, String message, Logger logger, String callableFormatVariable,
                      Class<? extends Exception>[] onExceptions, boolean reraise, String exceptionFormatVariable) {
        super(logLevel, message, logger, callableFormatVariable);
        this.onExceptions = onExceptions;
        this.reraise = reraise;
        this.exceptionFormatVariable = exceptionFormatVariable;
    }

    @Override
    public Object execute(Method fn, Object... args) throws Exception {
        try {
            return super.execute(fn, args);
        } catch (Exception e) {
            handleError(fn, e, args);
            if (reraise) {
                throw e;
            }
        }
        return null;
    }

    private void handleError(Method fn, Exception ex, Object... args) {
        for (Class<? extends Exception> exceptionClass : onExceptions) {
            if (exceptionClass.isInstance(ex)) {
                String msg = message.replace("{e!r}", ex.toString());
                log(logger, logLevel, msg);
            }
        }
    }
}
