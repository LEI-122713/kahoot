package iskahoot.server;

import iskahoot.model.Quiz;
import iskahoot.net.*;
import iskahoot.model.QuestionsFile;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketTimeoutException;

/**
 * Thread que trata UM cliente.
 * Usa ObjectInputStream/ObjectOutputStream para trocar objetos Message.
 * Fase 5: ciclo pergunta -> resposta -> placar, até acabar o quiz.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final GameManager gm;
    private final Quiz quiz;

    private String currentGame;
    private String currentUser;
    private String currentTeam;
    private GameRoom room;
    private GameSession session;
    private GameSession.ClientEndpoint endpoint;

    public ClientHandler(Socket s, GameManager gm, QuestionsFile qf) {
        this.socket = s;
        this.gm = gm;
        this.quiz = iskahoot.io.QuestionLoader.pickQuiz(qf);
    }

    @Override
    public void run() {
        try (Socket s = socket) {

            // IMPORTANTE: criar primeiro o ObjectOutputStream e fazer flush
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
            s.setSoTimeout(1000); // para poder sair se o jogo acabar

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

                JoinResponse resp = gm.handleJoin(join);
                if (resp.ok) {
                    currentGame = join.gameCode;
                    currentUser = join.username;
                    currentTeam = join.teamId;
                    room = gm.getRoom(currentGame);
                    session = gm.getOrCreateSession(currentGame, room, quiz);
                }

                // 3) Enviar resposta
                out.writeObject(resp);
                out.flush();

                // 3b) Enviar quiz completo para o cliente (carregado do servidor)
                if (resp.ok) {
                    QuizPayloadMessage qp = new QuizPayloadMessage(currentGame, quiz);
                    out.writeObject(qp);
                    out.flush();
                }

                if (resp.ok) {
                    endpoint = new GameSession.ClientEndpoint(out);
                    session.addClient(endpoint, currentTeam, currentUser);
                    listenLoop(in);
                }
            } else {
                System.out.println("Tipo de mensagem não suportado: " + msg.getClass());
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        } finally {
            gm.disconnectUser(currentGame, currentUser);
            if (session != null && endpoint != null) {
                session.removeClient(endpoint, currentUser);
            }
        }
    }

    private void listenLoop(ObjectInputStream in) throws IOException, ClassNotFoundException {
        while (true) {
            if (session != null && session.isFinished()) break;
            try {
                Message incoming = (Message) in.readObject();
                if (incoming instanceof AnswerMessage ans) {
                    session.handleAnswer(ans);
                } else {
                    // ignorar
                }
            } catch (SocketTimeoutException e) {
                // timeout para reavaliar se jogo terminou
                continue;
            }
        }
    }
}
