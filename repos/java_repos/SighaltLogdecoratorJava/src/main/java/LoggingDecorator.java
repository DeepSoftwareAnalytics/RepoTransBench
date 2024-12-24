import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class LoggingDecorator extends DecoratorMixin {
    protected final Level logLevel;
    protected final String message;
    protected Logger logger;
    protected String callableFormatVariable;

    public LoggingDecorator(Level logLevel, String message, Logger logger, String callableFormatVariable) {
        this.logLevel = logLevel;
        this.message = message;
        this.logger = logger;
        this.callableFormatVariable = callableFormatVariable;
    }

    @Override
    public Object execute(Method fn, Object... args) throws Exception {
        return fn.invoke(args[0], args);
    }

    protected void log(Logger logger, Level level, String msg) {
        logger.log(level, msg);
    }
}
