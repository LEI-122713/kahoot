package iskahoot.server;

import iskahoot.io.QuestionLoader;
import iskahoot.model.QuestionsFile;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Ponto de entrada do servidor: arranca a TUI, aceita sockets e lança um ClientHandler por cliente.
 * Usa por defeito a porta 6000 (pode ser alterado passando um argumento).
 */
public class Server {

    private static final int DEFAULT_PORT = 6000;

    public static void main(String[] args) throws Exception {

        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        GameManager gm = new GameManager();
        QuestionsFile qf = QuestionLoader.loadFromResource("/questions.json");
        new Thread(new ServerConsole(gm), "server-console").start();

        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Servidor a correr no porto " + port);
            while (true) {
                Socket s = ss.accept();
                System.out.println("Cliente ligado: " + s.getInetAddress());
                new Thread(new ClientHandler(s, gm, qf)).start();
            }
        }
    }
}
