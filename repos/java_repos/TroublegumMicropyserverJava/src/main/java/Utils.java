import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Map<Integer, String> HTTP_CODES = new HashMap<>();
    static {
        HTTP_CODES.put(100, "Continue");
        HTTP_CODES.put(101, "Switching protocols");
        HTTP_CODES.put(102, "Processing");
        HTTP_CODES.put(200, "Ok");
        // ... (the remaining codes)
        HTTP_CODES.put(511, "Network authentication required");
    }

    public static void sendResponse(MicroPyServer server, String response, int httpCode, String contentType, String[] extendHeaders) throws IOException {
        server.send("HTTP/1.0 " + httpCode + " " + HTTP_CODES.get(httpCode) + "\r\n");
        server.send("Content-Type:" + contentType + "\r\n");
        if (extendHeaders != null) {
            for (String header : extendHeaders) {
                server.send(header + "\r\n");
            }
        }
        server.send("\r\n");
        server.send(response);
    }

    public static String getRequestMethod(String request) {
        String[] lines = request.split("\r\n");
        Pattern pattern = Pattern.compile("^([A-Z]+)");
        Matcher matcher = pattern.matcher(lines[0]);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static String getRequestQueryString(String request) {
        String[] lines = request.split("\r\n");
        Pattern pattern = Pattern.compile("\\?(.+)\\s");
        Matcher matcher = pattern.matcher(lines[0]);
        return matcher.find() ? matcher.group(1) : "";
    }

    public static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        if (queryString.isEmpty()) return queryParams;

        String[] params = queryString.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : "";
            queryParams.put(key, value);
        }
        return queryParams;
    }

    public static Map<String, String> getRequestQueryParams(String request) {
        String queryString = getRequestQueryString(request);
        return parseQueryString(queryString);
    }

    public static Map<String, String> getRequestPostParams(String request) {
        String method = getRequestMethod(request);
        if (!"POST".equals(method)) return new HashMap<>();
        
        Pattern pattern = Pattern.compile("\r\n\r\n(.+)");
        Matcher matcher = pattern.matcher(request);
        if (matcher.find()) {
            return parseQueryString(matcher.group(1));
        }
        return new HashMap<>();
    }

    public static String unquote(String string) {
        if (string == null || string.isEmpty()) return "";
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

