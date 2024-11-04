import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LogOnEnd extends LoggingDecorator {
    private String resultFormatVariable;

    public LogOnEnd(Level logLevel, String message, Logger logger, String callableFormatVariable, String resultFormatVariable) {
        super(logLevel, message, logger, callableFormatVariable);
        this.resultFormatVariable = resultFormatVariable;
    }

    @Override
    public Object execute(Method fn, Object... args) throws Exception {
        Object result = super.execute(fn, args);
        doLogging(fn, result, args);
        return result;
    }

    private void doLogging(Method fn, Object result, Object... args) {
        String msg = message.replace("{result}", result.toString());
        log(logger, logLevel, msg);
    }
}
