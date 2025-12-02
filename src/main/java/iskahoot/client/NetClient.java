package iskahoot.client;

import iskahoot.net.JoinMessage;
import iskahoot.net.JoinResponse;
import iskahoot.net.Message;
import iskahoot.net.QuestionMessage;
import iskahoot.net.AnswerMessage;
import iskahoot.net.ScoreboardMessage;
import iskahoot.net.GameOverMessage;
import iskahoot.net.ErrorMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Cliente simples para testar a Fase 5.
 * Uso:
 *   java iskahoot.client.NetClient 127.0.0.1 5000 ABCD Team1 joao
 */
public class NetClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            System.out.println("Uso: java iskahoot.client.NetClient <IP> <PORTO> <JOGO> <EQUIPA> <USERNAME>");
            return;
        }

        String host     = args[0];
        int    port     = Integer.parseInt(args[1]);
        String gameCode = args[2];
        String teamId   = args[3];
        String username = args[4];

        try (Socket s = new Socket(host, port)) {
            System.out.println("Ligado ao servidor " + host + ":" + port);

            // Criar streams de objetos (mesma ordem do servidor)
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());

            // 1) Criar mensagem de join
            JoinMessage join = new JoinMessage(gameCode, teamId, username);

            // 2) Enviar
            out.writeObject(join);
            out.flush();
            System.out.println("Enviado pedido de join: " + gameCode + "," + teamId + "," + username);

            // 3) Ler resposta
            Object obj = in.readObject();
            if (obj instanceof JoinResponse resp) {
                System.out.println("Resposta do servidor: ok=" + resp.ok + ", info=" + resp.info);
                if (!resp.ok) return;
            } else if (obj instanceof Message) {
                System.out.println("Recebi outro tipo de mensagem: " + obj.getClass());
                return;
            } else {
                System.out.println("Objeto desconhecido do servidor.");
                return;
            }

            // 4) Esperar pergunta
            while (true) {
                Object recv = in.readObject();
                if (recv instanceof QuestionMessage qmsg) {
                    System.out.println("Pergunta [" + (qmsg.questionIndex + 1) + "/" + qmsg.totalQuestions + "]: " + qmsg.questionText);
                    for (int i = 0; i < qmsg.options.size(); i++) {
                        System.out.println("  " + i + ") " + qmsg.options.get(i));
                    }

                    // para teste: responde sempre com a opção 0
                    AnswerMessage ans = new AnswerMessage(gameCode, teamId, username, qmsg.questionIndex, 0);
                    out.writeObject(ans);
                    out.flush();
                    System.out.println("Resposta enviada (opção 0)");

                } else if (recv instanceof ScoreboardMessage sm) {
                    System.out.println("Placar: " + sm.scoreboard + " (" + sm.info + ")");
                } else if (recv instanceof GameOverMessage gm) {
                    System.out.println("Fim: " + gm.info);
                    break;
                } else if (recv instanceof ErrorMessage em) {
                    System.out.println("Erro: " + em.info);
                } else if (recv instanceof Message) {
                    System.out.println("Mensagem não tratada: " + recv.getClass());
                } else {
                    System.out.println("Objeto desconhecido do servidor.");
                    break;
                }
            }

        }
    }
}
