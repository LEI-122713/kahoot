package iskahoot.server;

import iskahoot.net.JoinMessage;
import iskahoot.net.JoinResponse;
import iskahoot.net.Message;

import java.io.*;
import java.net.Socket;

/**
 * Thread que trata UM cliente.
 * Agora usa ObjectInputStream/ObjectOutputStream para trocar objetos Message.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        try (Socket s = socket) {

            // IMPORTANTE: criar primeiro o ObjectOutputStream e fazer flush
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());

            // 1) Ler uma mensagem do cliente
            Object obj = in.readObject();
            if (!(obj instanceof Message msg)) {
                System.out.println("Mensagem desconhecida de " + s.getInetAddress());
                return;
            }

            // 2) Se for JoinMessage, tratar
            if (msg instanceof JoinMessage join) {
                System.out.println("Pedido JOIN -> jogo=" + join.gameCode +
                        ", equipa=" + join.teamId +
                        ", user=" + join.username);

                // Aqui no futuro vais validar jogo/equipa/username com o GameState/servidor
                // Por agora, aceitamos sempre:
                JoinResponse resp = new JoinResponse(true,
                        "Bem-vindo " + join.username + " à equipa " + join.teamId +
                                " (jogo " + join.gameCode + ")");

                // 3) Enviar resposta
                out.writeObject(resp);
                out.flush();
            } else {
                System.out.println("Tipo de mensagem não suportado: " + msg.getClass());
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        }
    }
}
