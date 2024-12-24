import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SplunkHandler extends AppenderBase<ILoggingEvent> {
    private static final String RECEIVER_ENDPOINT = "/services/collector/event";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private int maxQueueSize;
    private boolean allowOverrides;
    private final LinkedBlockingQueue<String> queue;
    private ScheduledFuture<?> schedulerFuture;
    private String splunkUrl;
    private String token;
    private String hostname;
    private String source;
    private String sourcetype;
    private String index;
    private boolean verify;
    private int timeout;
    private double flushInterval;
    private String host;
    private int port;
    private boolean debug; // Added this variable
    private int retryCount; // Added this variable
    private double retryBackoff; // Added this variable
    private boolean forceKeepAhead; // Added this variable

    public SplunkHandler(String host, int port, String token, String index, boolean allowOverrides, 
                         boolean debug, double flushInterval, boolean forceKeepAhead, String hostname, 
                         String protocol, String proxies, int queueSize, boolean recordFormat, 
                         double retryBackoff, int retryCount, String source, 
                         String sourcetype, int timeout, String url, boolean verify) {
        this.maxQueueSize = queueSize > 0 ? queueSize : 5000;
        this.allowOverrides = allowOverrides;
        this.queue = new LinkedBlockingQueue<>(maxQueueSize);
        this.token = token;
        this.index = index;
        this.source = source;
        this.sourcetype = sourcetype;
        this.verify = verify;
        this.timeout = timeout;
        this.flushInterval = flushInterval;
        this.splunkUrl = url != null ? url : protocol + "://" + host + ":" + port + RECEIVER_ENDPOINT;
        this.host = host;
        this.port = port;
        this.debug = debug; // Initialized this variable
        this.retryCount = retryCount; // Initialized this variable
        this.retryBackoff = retryBackoff; // Initialized this variable
        this.forceKeepAhead = forceKeepAhead; // Initialized this variable

        try {
            this.hostname = (hostname != null) ? hostname : InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            this.hostname = "unknown";
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .build();
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new StandardHttpRequestRetryHandler(retryCount, true))
                .build();

        startWorkerThread();
    }

    private void startWorkerThread() {
        this.schedulerFuture = scheduler.scheduleWithFixedDelay(this::sendLogs, 0, (long) (flushInterval * 1000), TimeUnit.MILLISECONDS);
    }

    private void sendLogs() {
        int batchSize = Math.min(10, queue.size());
        List<String> batch = new ArrayList<>();

        for (int i = 0; i < batchSize; i++) {
            batch.add(queue.poll());
        }

        if (!batch.isEmpty()) {
            String payload = String.join("", batch);

            HttpPost post = new HttpPost(splunkUrl);
            post.addHeader("Authorization", "Splunk " + token);
            post.setEntity(new StringEntity(payload, "UTF-8"));

            try (CloseableHttpClient httpclient = HttpClients.createSystem()) {
                HttpResponse httpResponse = this.httpClient.execute(post);
                if (httpResponse.getStatusLine().getStatusCode() >= 400) {
                    System.err.println("Failed to send logs to Splunk: " + httpResponse.getStatusLine().getReasonPhrase());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted() || shuttingDown.get()) {
            return;
        }

        if (this.flushInterval <= 0) {
            sendLogs();
            return;
        }
        try {
            if (!queue.offer(formatEvent(eventObject))) {
                System.err.println("Log queue full; log data will be dropped.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatEvent(ILoggingEvent event) {
        EventPayload payload = new EventPayload();
        payload.setEvent(event.getFormattedMessage());
        payload.setHost(hostname);
        payload.setIndex(index);
        payload.setSource(source != null ? source : event.getLoggerName());
        payload.setSourcetype(sourcetype);
        payload.setTime(event.getTimeStamp() / 1000.0);

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (shuttingDown.getAndSet(true)) {
            return;
        }
        schedulerFuture.cancel(true);
        scheduler.shutdown();
        sendLogs();
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitUntilEmpty() {
        while (!queue.isEmpty()) {
            sendLogs();
        }
    }

    public void writeLog(String message) {
        System.err.println(message);
    }

    // Added getter methods
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    // Added setter methods
    public void setAllowOverrides(boolean allowOverrides) {
        this.allowOverrides = allowOverrides;
    }

    public void setForceKeepAhead(boolean forceKeepAhead) {
        this.forceKeepAhead = forceKeepAhead;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    // Add required getter methods
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getToken() {
        return token;
    }

    public String getIndex() {
        return index;
    }

    public String getHostname() {
        return hostname;
    }

    public String getSource() {
        return source;
    }

    public String getSourcetype() {
        return sourcetype;
    }

    public boolean isVerify() {
        return verify;
    }

    public int getTimeout() {
        return timeout;
    }

    public double getFlushInterval() {
        return flushInterval;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public boolean isDebug() {
        return debug;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public double getRetryBackoff() {
        return retryBackoff;
    }
}

class EventPayload {
    private String event;
    private String host;
    private String index;
    private String source;
    private String sourcetype;
    private double time;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourcetype() {
        return sourcetype;
    }

    public void setSourcetype(String sourcetype) {
        this.sourcetype = sourcetype;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
