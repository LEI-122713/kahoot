package iskahoot.client;

import iskahoot.io.QuestionLoader;
import iskahoot.model.QuestionsFile;
import iskahoot.model.GameState;

import javax.swing.*;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    QuestionsFile qf = QuestionLoader.loadFromResource("/questions.json");
    // para a entrega intermédia basta 1 equipa
    GameState gs = new GameState(qf.questions, List.of("Team1"));
    SwingUtilities.invokeLater(() -> new KahootClientWindow(gs).setVisible(true));
  }
}
