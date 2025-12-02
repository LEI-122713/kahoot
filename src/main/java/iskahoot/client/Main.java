package iskahoot.client;

import iskahoot.io.QuestionLoader;
import iskahoot.model.QuestionsFile;
import iskahoot.model.GameState;
import iskahoot.model.Quiz;

import javax.swing.*;
import java.util.List;

// Entrada do cliente. Modo rede (args >=5): host port jogo equipa user -> GUI de rede.
// Caso contrário, corre o modo local offline da entrega intermédia.

public class Main {
  public static void main(String[] args) {
    if (args.length >= 5) {
      runNetwork(args);
    } else {
      runLocal(args);
    }
  }

  private static void runLocal(String[] args) {
    QuestionsFile qf = QuestionLoader.loadFromResource("/questions.json");
    Quiz quiz = QuestionLoader.pickQuiz(qf);

    String myTeam = (args.length >= 1) ? args[0] : "Team1";
    List<String> teams;
    if (args.length >= 2) {
      teams = new java.util.ArrayList<>(java.util.Arrays.asList(args).subList(1, args.length));
    } else {
      teams = new java.util.ArrayList<>(List.of("Team1", "Team2"));
    }

    if (!teams.contains(myTeam)) {
      teams.add(myTeam);
    }

    GameState gs = new GameState(quiz, teams);
    String finalMyTeam = myTeam;
    SwingUtilities.invokeLater(() -> new KahootClientWindow(gs, finalMyTeam).setVisible(true));
  }

  private static void runNetwork(String[] args) {
    String host     = args[0];
    int port        = Integer.parseInt(args[1]);
    String gameCode = args[2];
    String teamId   = args[3];
    String user     = args[4];

    try {
      NetGuiClient conn = NetGuiClient.connect(host, port, gameCode, teamId, user);
      SwingUtilities.invokeLater(() -> {
        NetworkKahootWindow w = new NetworkKahootWindow(conn, gameCode, teamId, user);
        w.setVisible(true);
      });
    } catch (Exception e) {
      System.err.println("Falha ao ligar: " + e.getMessage());
    }
  }
}
