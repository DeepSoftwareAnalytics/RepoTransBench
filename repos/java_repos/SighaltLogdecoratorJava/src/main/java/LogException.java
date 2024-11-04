import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LogException extends LogOnError {
    public LogException(String message, Logger logger, String callableFormatVariable, Class<? extends Exception>[] onExceptions,
                        boolean reraise, String exceptionFormatVariable) {
        super(Level.SEVERE, message, logger, callableFormatVariable, onExceptions, reraise, exceptionFormatVariable);
    }

    @Override
    protected void log(Logger logger, Level level, String msg) {
        logger.log(Level.SEVERE, msg);
    }
}
