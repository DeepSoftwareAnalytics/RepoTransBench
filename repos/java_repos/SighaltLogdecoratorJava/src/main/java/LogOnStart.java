import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LogOnStart extends LoggingDecorator {
    public LogOnStart(Level logLevel, String message, Logger logger, String callableFormatVariable) {
        super(logLevel, message, logger, callableFormatVariable);
    }

    @Override
    public Object execute(Method fn, Object... args) throws Exception {
        doLogging(fn, args);
        return super.execute(fn, args);
    }

    private void doLogging(Method fn, Object... args) {
        String msg = message.replace("{callable.__name__}", fn.getName());
        log(logger, logLevel, msg);
    }
}
