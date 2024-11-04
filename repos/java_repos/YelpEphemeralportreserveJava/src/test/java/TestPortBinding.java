import static org.junit.Assert.*;
import org.junit.Test;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.ServerSocket;

public class TestPortBinding {

    private static final String LOCALHOST = "127.0.0.1";

    private static int reserve(String ip) throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName(ip))) {
            return socket.getLocalPort();
        }
    }

    private static int reserve() throws IOException {
        return reserve(LOCALHOST);
    }

    private static Socket bindNaive(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            return socket;
        } catch (IOException e) {
            return null;
        }
    }

    private static ServerSocket bindReuse(String ip, int port) throws IOException {
        ServerSocket socket = new ServerSocket();
        socket.setReuseAddress(true);
        socket.bind(new java.net.InetSocketAddress(ip, port));
        return socket;
    }

    private static void assertIp(String ip) throws IOException {
        int port = reserve(ip);
        Socket errorSocket = bindNaive(ip, port);
        assertNull(errorSocket);

        ServerSocket sock = bindReuse(ip, port);
        assertEquals(ip, sock.getInetAddress().getHostAddress());
        assertEquals(port, sock.getLocalPort());
    }

    @Test
    public void testLocalhost() throws IOException {
        assertIp(LOCALHOST);
    }

    @Test
    public void testFqdn() throws IOException {
        String fqdn = InetAddress.getLocalHost().getHostName();
        String fqip = InetAddress.getByName(fqdn).getHostAddress();
        assertIp(fqip);
    }

    @Test
    public void testPreferredPort() throws IOException {
        int port = reserve();
        int port2 = reserve(LOCALHOST);
        assertEquals(port, port2);
        assertNotNull(bindReuse(LOCALHOST, port2));
    }

    @Test
    public void testPreferredPortInUse() throws IOException {
        int port = reserve();
        ServerSocket sock = bindReuse(LOCALHOST, port);
        int port2 = reserve(LOCALHOST);
        assertNotEquals(port, port2);
        assertNotNull(bindReuse(LOCALHOST, port2));
    }
}
