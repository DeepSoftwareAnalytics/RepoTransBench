import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class EphemeralPortReserve {
    private static final String LOCALHOST = "127.0.0.1";

    public static int reserve(String ip, int port) throws IOException {
        port = port > 0 ? port : 0;
        InetSocketAddress endpoint = new InetSocketAddress(InetAddress.getByName(ip), port);
        
        try (ServerSocket s = new ServerSocket()) {
            s.setReuseAddress(true);
            try {
                s.bind(endpoint);
            } catch (SocketException e) {
                if (e.getMessage().contains("Address already in use") && port != 0) {
                    endpoint = new InetSocketAddress(InetAddress.getByName(ip), 0);
                    s.bind(endpoint);
                } else {
                    throw e;
                }
            }

            // Removed incorrect 'listen(int)' method, no equivalent required in Java
            // Changed method of validating port binding
            InetSocketAddress sockname = (InetSocketAddress) s.getLocalSocketAddress();

            try (Socket s2 = new Socket(sockname.getAddress(), sockname.getPort())) {
                // Correctly ensure server is bound by accepting one connection
                try (Socket ignored = s.accept()) {
                    return sockname.getPort();
                }
            }
        }
    }

    public static int reserve(String ip) throws IOException {
        return reserve(ip, 0);
    }

    public static int reserve() throws IOException {
        return reserve(LOCALHOST, 0);
    }

    public static ServerSocket bindReuse(String ip, int port) throws IOException {
        ServerSocket socket = new ServerSocket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(ip, port));
        return socket;
    }

    public static void main(String[] args) {
        try {
            int port = args.length > 0 ? reserve(args[0], Integer.parseInt(args[1])) : reserve();
            System.out.println(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
