import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;

public class CheckHttpJson {

    private static final int OK_CODE = 0;
    private static final int WARNING_CODE = 1;
    private static final int CRITICAL_CODE = 2;
    private static final int UNKNOWN_CODE = 3;

    private static final String VERSION = "2.2.0";
    private static final String VERSION_DATE = "2024-05-14";

    public static void main(String[] args) {
        new CheckHttpJson().run(args);
    }

    public void run(String[] args) {
        CommandLineArguments arguments = CommandLineArguments.parseArgs(args);
        NagiosHelper nagios = new NagiosHelper();
        SSLContext context = null;

        if (arguments.isVersion()) {
            System.out.printf("Version: %s - Date: %s%n", VERSION, VERSION_DATE);
            System.exit(0);
        }

        String url = arguments.isSsl() ? "https://" + arguments.getHost() : "http://" + arguments.getHost();
        if (arguments.getPort() != null) {
            url += ":" + arguments.getPort();
        }
        if (arguments.getPath() != null) {
            url += "/" + arguments.getPath();
        }

        debugPrint(arguments.isDebug(), "url: " + url);

        try {
            if (arguments.isSsl()) {
                context = prepareContext(arguments, nagios);
            }
            String jsonData = makeRequest(arguments, url, context);
            processResponse(arguments, jsonData, nagios);
        } catch (Exception e) {
            nagios.appendMessage(UNKNOWN_CODE, "Error: " + e.getMessage());
        }

        System.out.println(nagios.getMessage());
        System.exit(nagios.getCode());
    }

    private void processResponse(CommandLineArguments arguments, String jsonData, NagiosHelper nagios) {
        try {
            JSONObject data = new JSONObject(jsonData);
            verbosePrint(arguments.getVerbose(), 1, data.toString(2));

            JsonRuleProcessor processor = new JsonRuleProcessor(data, arguments);
            nagios.appendMessage(WARNING_CODE, processor.checkWarning());
            nagios.appendMessage(CRITICAL_CODE, processor.checkCritical());

            String[] metrics = processor.checkMetrics();
            nagios.appendMetrics(metrics[0], metrics[1], metrics[2]);
            
            nagios.appendMessage(UNKNOWN_CODE, processor.checkUnknown());
        } catch (Exception e) {
            nagios.appendMessage(UNKNOWN_CODE, "JSON Parser error: " + e.getMessage());
        }
    }

    private String makeRequest(CommandLineArguments arguments, String url, SSLContext context) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(arguments.getMethod());
        connection.setRequestProperty("User-Agent", "check_http_json");

        if (arguments.getAuth() != null) {
            String encodedAuth = Base64.getEncoder().encodeToString(arguments.getAuth().getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
        }

        if (arguments.getHeaders() != null) {
            for (Map.Entry<String, String> entry : arguments.getHeaders().entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        if (arguments.getTimeout() != null) {
            connection.setConnectTimeout(arguments.getTimeout() * 1000);
        }

        if (arguments.getData() != null) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = arguments.getData().getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }

        int statusCode = connection.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            content.append(line);
        }
        in.close();

        return content.toString();
    }

    private SSLContext prepareContext(CommandLineArguments arguments, NagiosHelper nagios) throws Exception {
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = null;

        if (arguments.isInsecure()) {
            trustManagers = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }};
        } else {
            context.init(null, null, new java.security.SecureRandom());
        }

        context.init(null, trustManagers, new java.security.SecureRandom());
        return context;
    }

    private void debugPrint(boolean debugFlag, String message) {
        if (debugFlag) {
            System.out.println(message);
        }
    }

    private void verbosePrint(int verboseFlag, int when, String message) {
        if (verboseFlag >= when) {
            System.out.println(message);
        }
    }
}
