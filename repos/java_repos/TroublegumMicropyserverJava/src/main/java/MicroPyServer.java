import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MicroPyServer {

    private String host;
    private int port;
    private List<Route> routes;
    private Socket connect;
    private OnRequestHandler onRequestHandler;
    private OnNotFoundHandler onNotFoundHandler;
    private OnErrorHandler onErrorHandler;
    private ServerSocket serverSocket;

    public MicroPyServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.routes = new ArrayList<>();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server start");
        while (!serverSocket.isClosed()) {
            connect = serverSocket.accept();
            String request = getRequest();
            if (request.isEmpty()) {
                connect.close();
                continue;
            }
            if (onRequestHandler != null && !onRequestHandler.handle(request)) {
                continue;
            }
            Route route = findRoute(request);
            if (route != null) {
                route.handler.handle(request);
            } else {
                routeNotFound(request);
            }
        }
    }

    public void stop() throws IOException {
        if (connect != null) {
            connect.close();
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
        System.out.println("Server stop");
    }

    public void send(String data) throws IOException {
        if (connect == null) throw new IOException("Can't send response, no connection instance");
        connect.getOutputStream().write(data.getBytes());
    }

    public void addRoute(String path, Handler handler, String method) {
        this.routes.add(new Route(path, handler, method));
    }

    private Route findRoute(String request) {
        String[] lines = request.split("\r\n");
        String method = null;
        String path = null;
        try {
            Pattern methodPattern = Pattern.compile("^([A-Z]+)");
            Pattern pathPattern = Pattern.compile("^[A-Z]+\\s+(/[-a-zA-Z0-9_.]*)");

            Matcher methodMatcher = methodPattern.matcher(lines[0]);
            Matcher pathMatcher = pathPattern.matcher(lines[0]);

            if (methodMatcher.find()) {
                method = methodMatcher.group(1);
            }
            if (pathMatcher.find()) {
                path = pathMatcher.group(1);
            }

            if (method == null || path == null) {
                return null;
            }

        } catch (Exception e) {
            return null;
        }

        for (Route route : routes) {
            if (method.equals(route.method)) {
                Pattern routePattern = Pattern.compile("^" + route.path + "$");
                Matcher match = routePattern.matcher(path);
                if (match.find()) {
                    return route;
                }
            }
        }
        return null;
    }

    private String getRequest() throws IOException {
        byte[] buffer = new byte[4096];
        int read = connect.getInputStream().read(buffer);
        return new String(buffer, 0, read, "UTF-8");
    }

    private void routeNotFound(String request) throws IOException {
        if (onNotFoundHandler != null) {
            onNotFoundHandler.handle(request);
        } else {
            send("HTTP/1.0 404 Not Found\r\n");
            send("Content-Type: text/plain\r\n\r\n");
            send("Not found");
        }
    }

    public void onRequest(OnRequestHandler handler) {
        this.onRequestHandler = handler;
    }

    public void onNotFound(OnNotFoundHandler handler) {
        this.onNotFoundHandler = handler;
    }

    public void onError(OnErrorHandler handler) {
        this.onErrorHandler = handler;
    }

    private void internalError(Exception error) throws IOException {
        if (onErrorHandler != null) {
            onErrorHandler.handle(error);
        } else {
            String strError = error.getMessage();
            send("HTTP/1.0 500 Internal Server Error\r\n");
            send("Content-Type: text/plain\r\n\r\n");
            send("Error: " + strError);
            error.printStackTrace();
        }
    }

    public static interface Handler {
        void handle(String request) throws IOException;
    }

    public static interface OnRequestHandler {
        boolean handle(String request) throws IOException;
    }

    public static interface OnNotFoundHandler {
        void handle(String request) throws IOException;
    }

    public static interface OnErrorHandler {
        void handle(Exception error) throws IOException;
    }

    private class Route {
        String path;
        Handler handler;
        String method;

        Route(String path, Handler handler, String method) {
            this.path = path;
            this.handler = handler;
            this.method = method;
        }
    }
}
