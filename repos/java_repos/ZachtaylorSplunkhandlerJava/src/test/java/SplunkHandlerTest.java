import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.apache.http.client.methods.HttpPost;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SplunkHandlerTest {
    private SplunkHandler splunkHandler;
    private String SPLUNK_HOST = "splunk-server.example.com";
    private int SPLUNK_PORT = 1234;
    private String SPLUNK_TOKEN = "851A5E58-4EF1-7291-F947-F614A76ACB21";
    private String SPLUNK_INDEX = "test_index";
    private String SPLUNK_HOSTNAME = "test_host";
    private String SPLUNK_SOURCE = "test_source";
    private String SPLUNK_SOURCETYPE = "test_sourcetype";
    private boolean SPLUNK_VERIFY = false;
    private int SPLUNK_TIMEOUT = 27;
    private double SPLUNK_FLUSH_INTERVAL = 0.1;
    private int SPLUNK_QUEUE_SIZE = 1111;
    private boolean SPLUNK_DEBUG = false;
    private int SPLUNK_RETRY_COUNT = 1;
    private double SPLUNK_RETRY_BACKOFF = 0.1;

    private String RECEIVER_URL = "https://" + SPLUNK_HOST + ":" + SPLUNK_PORT + "/services/collector/event";

    @Before
    public void setUp() {
        splunkHandler = Mockito.spy(new SplunkHandler(SPLUNK_HOST, SPLUNK_PORT, SPLUNK_TOKEN, SPLUNK_INDEX, false,
                SPLUNK_DEBUG, SPLUNK_FLUSH_INTERVAL, false, SPLUNK_HOSTNAME, "https", null,
                SPLUNK_QUEUE_SIZE, false, SPLUNK_RETRY_BACKOFF, SPLUNK_RETRY_COUNT,
                SPLUNK_SOURCE, SPLUNK_SOURCETYPE, SPLUNK_TIMEOUT, null, SPLUNK_VERIFY));
    }

    @Test
    public void testInit() {
        assertNotNull(splunkHandler);
        assertEquals(SPLUNK_HOST, splunkHandler.getHost());
        assertEquals(SPLUNK_PORT, splunkHandler.getPort());
        assertEquals(SPLUNK_TOKEN, splunkHandler.getToken());
        assertEquals(SPLUNK_INDEX, splunkHandler.getIndex());
        assertEquals(SPLUNK_HOSTNAME, splunkHandler.getHostname());
        assertEquals(SPLUNK_SOURCE, splunkHandler.getSource());
        assertEquals(SPLUNK_SOURCETYPE, splunkHandler.getSourcetype());
        assertEquals(SPLUNK_VERIFY, splunkHandler.isVerify());
        assertEquals(SPLUNK_TIMEOUT, splunkHandler.getTimeout());
        assertEquals(SPLUNK_FLUSH_INTERVAL, splunkHandler.getFlushInterval(), 0);
        assertEquals(SPLUNK_QUEUE_SIZE, splunkHandler.getMaxQueueSize());
        assertEquals(SPLUNK_DEBUG, splunkHandler.isDebug());
        assertEquals(SPLUNK_RETRY_COUNT, splunkHandler.getRetryCount());
        assertEquals(SPLUNK_RETRY_BACKOFF, splunkHandler.getRetryBackoff(), 0.01);

        assertFalse(((Logger) LoggerFactory.getLogger("requests")).isAdditive());
        assertFalse(((Logger) LoggerFactory.getLogger("splunk_handler")).isAdditive());
    }

    @Test
    public void testSplunkWorker() {
        Logger log = (Logger) LoggerFactory.getLogger(SplunkHandlerTest.class);
        log.setLevel(Level.WARN);
        log.addAppender(splunkHandler);

        log.warn("hello!");

        try {
            TimeUnit.SECONDS.sleep(1);  // Have to wait for the timer to execute
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
        try {
            verify(splunkHandler.getHttpClient(), times(1)).execute(argument.capture());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String expectedOutput = "{\"event\":\"hello!\",\"host\":\"" + SPLUNK_HOSTNAME + "\",\"index\":\"" + SPLUNK_INDEX + "\",\"source\":\"" + SPLUNK_SOURCE + "\",\"sourcetype\":\"" + SPLUNK_SOURCETYPE + "\",\"time\":10}";
        assertEquals(RECEIVER_URL, argument.getValue().getURI().toString());
        assertEquals("Splunk " + SPLUNK_TOKEN, argument.getValue().getFirstHeader("Authorization").getValue());

        // Comparing JSON strings for equivalency
        try {
            assertEquals(expectedOutput, new String(argument.getValue().getEntity().getContent().readAllBytes(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSplunkWorkerOverride() {
        splunkHandler.setAllowOverrides(true);

        Logger log = (Logger) LoggerFactory.getLogger(SplunkHandlerTest.class);
        log.setLevel(Level.WARN);
        log.addAppender(splunkHandler);

        log.warn("hello!", new Object[]{"_time", 5, "_host", "host", "_index", "index"});

        try {
            TimeUnit.SECONDS.sleep(1);  // Have to wait for the timer to execute
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
        try {
            verify(splunkHandler.getHttpClient(), times(1)).execute(argument.capture());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String expectedOutput = "{\"event\":\"hello!\",\"host\":\"host\",\"index\":\"index\",\"source\":\"" + SPLUNK_SOURCE + "\",\"sourcetype\":\"" + SPLUNK_SOURCETYPE + "\",\"time\":5}";
        assertEquals(RECEIVER_URL, argument.getValue().getURI().toString());
        assertEquals("Splunk " + SPLUNK_TOKEN, argument.getValue().getFirstHeader("Authorization").getValue());

        // Comparing JSON strings for equivalency
        try {
            assertEquals(expectedOutput, new String(argument.getValue().getEntity().getContent().readAllBytes(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFullQueueError() {
        splunkHandler.setAllowOverrides(true);
        splunkHandler.setMaxQueueSize(10);

        Logger log = (Logger) LoggerFactory.getLogger(SplunkHandlerTest.class);
        log.setLevel(Level.WARN);
        log.addAppender(splunkHandler);

        for (int i = 0; i < 20; i++) {
            log.warn("hello!", new Object[]{"_time", 5, "_host", "host", "_index", "index"});
        }

        try {
            TimeUnit.SECONDS.sleep(1);  // Have to wait for the timer to execute
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        verify(splunkHandler).writeLog("Log queue full; log data will be dropped.");
    }

    @Test
    public void testWaitUntilEmptyAndKeepAhead() {
        splunkHandler.setAllowOverrides(true);
        splunkHandler.setForceKeepAhead(true);
        splunkHandler.setMaxQueueSize(10);

        Logger log = (Logger) LoggerFactory.getLogger(SplunkHandlerTest.class);
        log.setLevel(Level.WARN);
        log.addAppender(splunkHandler);

        for (int i = 0; i < 20; i++) {
            log.warn("hello!", new Object[]{"_time", 5, "_host", "host", "_index", "index"});
        }

        splunkHandler.waitUntilEmpty();

        String expectedOutput = "{\"event\":\"hello!\",\"host\":\"host\",\"index\":\"index\",\"source\":\"" + SPLUNK_SOURCE + "\",\"sourcetype\":\"" + SPLUNK_SOURCETYPE + "\",\"time\":5}";

        ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
        try {
            verify(splunkHandler.getHttpClient(), times(2)).execute(argument.capture());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(RECEIVER_URL, argument.getValue().getURI().toString());
        assertEquals("Splunk " + SPLUNK_TOKEN, argument.getValue().getFirstHeader("Authorization").getValue());
        verify(splunkHandler, times(0)).writeLog(anyString());
    }
}

