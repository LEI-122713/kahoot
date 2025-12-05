package iskahoot.client;

import iskahoot.net.JoinMessage;
import iskahoot.net.JoinResponse;
import iskahoot.net.Message;
import iskahoot.net.QuizPayloadMessage;
import iskahoot.model.Quiz;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Cliente de rede (GUI) responsável pelo handshake de join e entrega de streams. */
public class NetGuiClient implements AutoCloseable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private Quiz quiz;

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
            // esperar quiz payload imediatamente a seguir
            Object maybeQuiz = in.readObject();
            Quiz loadedQuiz = null;
            if (maybeQuiz instanceof QuizPayloadMessage qp) {
                loadedQuiz = qp.quiz;
            }
            NetGuiClient client = new NetGuiClient(s, out, in);
            client.quiz = loadedQuiz;
            return client;
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

    public Quiz quiz() {
        return quiz;
    }

    @Override
    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
