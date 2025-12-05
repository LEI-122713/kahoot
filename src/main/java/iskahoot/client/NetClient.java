package iskahoot.client;

import iskahoot.net.JoinMessage;
import iskahoot.net.JoinResponse;
import iskahoot.net.Message;
import iskahoot.net.QuestionMessage;
import iskahoot.net.AnswerMessage;
import iskahoot.net.ScoreboardMessage;
import iskahoot.net.GameOverMessage;
import iskahoot.net.ErrorMessage;
import iskahoot.net.QuizPayloadMessage;
import iskahoot.model.Quiz;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Cliente de consola para testar a rede (envia respostas automáticas). */
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

            // 4) Esperar quiz completo e depois perguntas
            Quiz quiz = null;
            while (true) {
                Object recv = in.readObject();
                if (recv instanceof QuizPayloadMessage qp) {
                    quiz = qp.quiz;
                    System.out.println("Quiz recebido do servidor: " + (quiz != null ? quiz.name : "sem nome"));
                    if (quiz != null) {
                        System.out.println("Total de perguntas: " + quiz.questions.size());
                    }
                } else if (recv instanceof QuestionMessage qmsg) {
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
                    if (sm.roundPoints != null) {
                        System.out.println("Pontos da ronda: " + sm.roundPoints);
                    }
                    if (sm.ranking != null) {
                        System.out.println("Ranking: " + sm.ranking);
                        int pos = sm.ranking.indexOf(teamId);
                        if (pos >= 0) {
                            System.out.println("Posição da equipa: " + (pos + 1));
                        }
                    }
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
