import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestDecorators {
    private Logger logger;
    private MockLoggingHandler logHandler;

    @Mock
    private TestFunction testFunction;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        logger = Logger.getLogger("mocked");
        logHandler = new MockLoggingHandler();
        logger.addHandler(logHandler);
    }

    @Test
    public void testLogOnStart() throws Exception {
        LogOnStart logOnStart = new LogOnStart(Level.INFO, "test message {arg1}, {arg2}", logger, "callable");
        Method method = testFunction.getClass().getMethod("testFunc", int.class, int.class);
        logOnStart.execute(method, testFunction, 1, 2);
        assertTrue(logHandler.getMessages().contains("test message 1, 2"));
    }

    @Test
    public void testAsyncLogOnStart() throws Exception {
        AsyncLogOnStart asyncLogOnStart = new AsyncLogOnStart(Level.INFO, "test message {arg1}, {arg2}", logger, "callable");
        Method method = testFunction.getClass().getMethod("asyncTestFunc", int.class, int.class);
        asyncLogOnStart.executeAsync(method, testFunction, 1, 2).get();
        assertTrue(logHandler.getMessages().contains("test message 1, 2"));
    }

    @Test
    public void testLogOnEnd() throws Exception {
        LogOnEnd logOnEnd = new LogOnEnd(Level.INFO, "test message {arg1}, {arg2} => {result}", logger, "callable");
        Method method = testFunction.getClass().getMethod("testFunc", int.class, int.class);
        logOnEnd.execute(method, testFunction, 1, 2);
        assertTrue(logHandler.getMessages().contains("test message 1, 2 => 3"));
    }

    @Test
    public void testAsyncLogOnEnd() throws Exception {
        AsyncLogOnEnd asyncLogOnEnd = new AsyncLogOnEnd(Level.INFO, "test message {arg1}, {arg2} => {result}", logger, "callable");
        Method method = testFunction.getClass().getMethod("asyncTestFunc", int.class, int.class);
        asyncLogOnEnd.executeAsync(method, testFunction, 1, 2).get();
        assertTrue(logHandler.getMessages().contains("test message 1, 2 => 3"));
    }

    @Test
    public void testLogOnError() throws Exception {
        Method method = mock(Method.class);
        when(testFunction.testFunc(anyInt(), anyInt())).thenThrow(new TestException("test exception"));
        
        LogOnError logOnError = new LogOnError(Level.INFO, "test message {e}", logger, "callable");
        logOnError.execute(method, testFunction, 1, 2);
        
        assertTrue(logHandler.getMessages().contains("test message test exception"));
    }

    @Test
    public void testAsyncLogOnError() throws Exception {
        Method method = mock(Method.class);
        when(testFunction.asyncTestFunc(anyInt(), anyInt())).thenThrow(new TestException("test exception"));
        
        AsyncLogOnError asyncLogOnError = new AsyncLogOnError(Level.INFO, "test message {e}", logger, "callable");
        asyncLogOnError.executeAsync(method, testFunction, 1, 2).get();
        
        assertTrue(logHandler.getMessages().contains("test message test exception"));
    }

    @Test
    public void testLogException() throws Exception {
        Method method = mock(Method.class);
        when(testFunction.testFunc(anyInt(), anyInt())).thenThrow(new TypeError("test type error"));

        LogException logException = new LogException("test message", logger, TypeError.class);
        logException.execute(method, testFunction, 1, 2);

        assertEquals(1, logger.getHandlers().length);
    }

    @Test
    public void testAsyncLogException() throws Exception {
        Method method = mock(Method.class);
        when(testFunction.asyncTestFunc(anyInt(), anyInt())).thenThrow(new TypeError("test type error"));

        AsyncLogException asyncLogException = new AsyncLogException("test message", logger, TypeError.class);
        asyncLogException.executeAsync(method, testFunction, 1, 2).get();

        assertEquals(1, logger.getHandlers().length);
    }

    @Test
    public void testCallableNameVariable() throws Exception {
        LogOnStart logOnStart = new LogOnStart(Level.INFO, "{callable}", logger, "callable");
        Method method = testFunction.getClass().getMethod("testFunc", int.class, int.class);
        logOnStart.execute(method, testFunction, 1, 2);
        assertTrue(logHandler.getMessages().contains("testFunc"));
    }

    @Test
    public void testCustomCallableNameVariable() throws Exception {
        LogOnStart logOnStart = new LogOnStart(Level.INFO, "{mycallable}", logger, "mycallable");
        Method method = testFunction.getClass().getMethod("testFunc", int.class, int.class);
        logOnStart.execute(method, testFunction, 1, 2);
        assertTrue(logHandler.getMessages().contains("testFunc"));
    }

    @Test
    public void testCustomHandler() throws Exception {
        LogOnStart logOnStart = new LogOnStart(Level.ERROR, "test message {arg1}, {arg2}", logHandler);
        Method method = testFunction.getClass().getMethod("testFunc", int.class, int.class);
        logOnStart.execute(method, testFunction, 1, 2);
        assertTrue(logHandler.getMessages().contains("test message 1, 2"));
    }

    @Test
    public void testOmittedOptionalParameters() throws Exception {
        LogOnStart logOnStart = new LogOnStart(Level.ERROR, "test message {kwarg1}", logHandler);
        Method method = testFunction.getClass().getMethod("testFunc", int.class, int.class);
        logOnStart.execute(method, testFunction, 1, 2);
        assertTrue(logHandler.getMessages().contains("test message null"));
    }
}
