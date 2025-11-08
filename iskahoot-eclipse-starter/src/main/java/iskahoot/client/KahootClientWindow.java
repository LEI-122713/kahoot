package iskahoot.client;

import iskahoot.model.Question;
import iskahoot.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class KahootClientWindow extends JFrame {
  private final JLabel lblTitle = new JLabel("IsKahoot — Cliente (Entrega Intermédia)");
  private final JLabel lblQuestion = new JLabel("Pergunta");
  private final JList<String> listOptions = new JList<>();
  private final OptionsListModel optionsModel = new OptionsListModel();
  private final JButton btnAnswer = new JButton("Responder");
  private final JLabel lblInfo = new JLabel("Placar: --");

  // equipa fixada para a entrega (podes trocar para "Team2" se quiseres)
  private final String myTeam = "Team1";
  private final GameState gs;

  public KahootClientWindow(GameState gs) {
    super("IsKahoot — Cliente");
    this.gs = gs;
    initUI();
    showQuestion();
    refreshScoreboard();
  }

  private void initUI() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(8,8));

    lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
    lblQuestion.setFont(lblQuestion.getFont().deriveFont(Font.BOLD, 18f));
    add(lblTitle, BorderLayout.NORTH);

    JPanel center = new JPanel(new BorderLayout(8,8));
    center.add(lblQuestion, BorderLayout.NORTH);

    listOptions.setModel(optionsModel);
    listOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    center.add(new JScrollPane(listOptions), BorderLayout.CENTER);

    JPanel south = new JPanel(new BorderLayout());
    south.add(lblInfo, BorderLayout.NORTH);
    south.add(btnAnswer, BorderLayout.SOUTH);
    add(center, BorderLayout.CENTER);
    add(south, BorderLayout.SOUTH);

    btnAnswer.addActionListener(e -> onAnswer());

    setSize(640, 420);
    setLocationRelativeTo(null);
  }

  private void showQuestion() {
    if (!gs.hasNext()) {
      lblQuestion.setText("Fim do quiz!");
      optionsModel.setOptions(java.util.Collections.emptyList());
      btnAnswer.setEnabled(false);
      return;
    }
    Question q = gs.current();
    lblQuestion.setText("<html>" + q.question + " (" + q.points + " pts)</html>");
    optionsModel.setOptions(q.options);
    listOptions.clearSelection();
  }

  private void onAnswer() {
    int sel = listOptions.getSelectedIndex();
    if (sel < 0) {
      JOptionPane.showMessageDialog(this, "Escolhe uma opção primeiro.");
      return;
    }

    // atualiza placar local
    gs.submitAnswer(myTeam, sel);

    // feedback simples
    boolean correct = (sel == gs.current().correct);
    JOptionPane.showMessageDialog(this, correct ? "Certo!" : "Errado.");

    gs.next();
    showQuestion();
    refreshScoreboard();
  }

  private void refreshScoreboard() {
    StringBuilder sb = new StringBuilder("Placar: ");
    for (Map.Entry<String,Integer> e : gs.getScoreboard().entrySet()) {
      sb.append(e.getKey()).append(": ").append(e.getValue()).append("  ");
    }
    lblInfo.setText(sb.toString());
  }
}
