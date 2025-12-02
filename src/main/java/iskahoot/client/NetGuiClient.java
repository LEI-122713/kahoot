package iskahoot.client;

import iskahoot.net.JoinMessage;
import iskahoot.net.JoinResponse;
import iskahoot.net.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Cliente de rede para ligar ao servidor e efetuar o handshake de join.
 */
public class NetGuiClient implements AutoCloseable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private NetGuiClient(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    public static NetGuiClient connect(String host, int port, String gameCode, String teamId, String username) throws Exception {
        Socket s = new Socket(host, port);
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        out.flush();
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());

        JoinMessage join = new JoinMessage(gameCode, teamId, username);
        out.writeObject(join);
        out.flush();

        Object obj = in.readObject();
        if (obj instanceof JoinResponse resp) {
            if (!resp.ok) {
                s.close();
                throw new IllegalStateException("Join falhou: " + resp.info);
            }
            return new NetGuiClient(s, out, in);
        }
        s.close();
        throw new IllegalStateException("Resposta inesperada do servidor");
    }

    public ObjectOutputStream out() {
        return out;
    }

    public ObjectInputStream in() {
        return in;
    }

    @Override
    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
