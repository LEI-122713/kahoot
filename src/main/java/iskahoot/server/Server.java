package iskahoot.server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor simples da fase 4/5.
 * Aceita ligações e cria uma thread ClientHandler para cada cliente.
 */
public class Server {

    private static final int PORT = 5000;

    public static void main(String[] args) throws Exception {
        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("Servidor a correr no porto " + PORT);

            while (true) {
                Socket s = ss.accept();
                System.out.println("Cliente ligado: " + s.getInetAddress());
                new Thread(new ClientHandler(s)).start();
            }
        }
    }
}
